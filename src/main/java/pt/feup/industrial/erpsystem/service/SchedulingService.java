package pt.feup.industrial.erpsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.erpsystem.dto.MesProductionOrderDto;
import pt.feup.industrial.erpsystem.mes.MesClientService;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import pt.feup.industrial.erpsystem.model.OrderItem;
import pt.feup.industrial.erpsystem.model.OrderItemStatus;
import pt.feup.industrial.erpsystem.model.OrderStatus;
import pt.feup.industrial.erpsystem.repository.ClientOrderRepository;
import pt.feup.industrial.erpsystem.repository.OrderItemRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final ClientOrderRepository clientOrderRepository;
    private final MesClientService mesClientService;
    private final OrderItemRepository orderItemRepository;

    private static final int DAILY_FACTORY_PIECE_CAPACITY = 24;

    @Autowired
    public SchedulingService(ClientOrderRepository clientOrderRepository, MesClientService mesClientService, OrderItemRepository orderItemRepository) {
        this.clientOrderRepository = clientOrderRepository;
        this.mesClientService = mesClientService;
        this.orderItemRepository = orderItemRepository;
    }

    @Scheduled(cron = "${erp.scheduling.mes-sync-cron:0 0 9 * * *}")
    @Transactional // One transaction for the whole daily scheduling run
    public void sendPendingOrdersToMes() {
        log.info("Daily Scheduling Task: Starting to process orders for MES.");

        List<OrderItem> sortedPendingItems = orderItemRepository.findPendingItemsSortedByDueDate(
                OrderItemStatus.PENDING, OrderStatus.PENDING
        );

        if (sortedPendingItems.isEmpty()) {
            log.info("Daily Scheduling Task: No PENDING items found to schedule.");
            return;
        }

        log.info("Daily Scheduling Task: Found {} PENDING items to consider.", sortedPendingItems.size());

        int piecesScheduledThisRun = 0;
        Set<Long> affectedClientOrderIds = new HashSet<>();

        for (OrderItem item : sortedPendingItems) {
            if (piecesScheduledThisRun + item.getQuantity() > DAILY_FACTORY_PIECE_CAPACITY) {
                log.info("Daily Scheduling Task: Daily factory capacity ({}) reached or would be exceeded. " +
                                "Stopping further scheduling for this run. Pieces scheduled so far: {}.",
                        DAILY_FACTORY_PIECE_CAPACITY, piecesScheduledThisRun);
                break; // Stop processing more items for this run
            }

            ClientOrder parentOrder = item.getClientOrder();
            if (parentOrder.getStatus() != OrderStatus.PENDING) {
                log.warn("Item ID {} belongs to Order ID {} which is no longer PENDING (Status: {}). Skipping item.",
                        item.getId(), parentOrder.getId(), parentOrder.getStatus());
                continue;
            }

            log.debug("Processing Item ID {} (Due: {}, Qty: {}) from Order ID {}",
                    item.getId(), item.getDueDate(), item.getQuantity(), parentOrder.getId());

            MesProductionOrderDto mesRequest = new MesProductionOrderDto(
                    parentOrder.getId(), item.getId(), item.getProductType(), item.getQuantity(), item.getDueDate());

            boolean sentSuccessfully = mesClientService.sendProductionOrder(mesRequest);

            if (sentSuccessfully) {
                item.setStatus(OrderItemStatus.SENT_TO_MES);
                orderItemRepository.save(item); // Persist item status change
                piecesScheduledThisRun += item.getQuantity();
                affectedClientOrderIds.add(parentOrder.getId());
                log.info("Successfully sent Item ID {} to MES (Qty: {}). Status -> SENT_TO_MES. Total pieces scheduled this run: {}",
                        item.getId(), item.getQuantity(), piecesScheduledThisRun);
            } else {
                item.setStatus(OrderItemStatus.FAILED_TO_SEND);
                orderItemRepository.save(item); // Persist item status change
                affectedClientOrderIds.add(parentOrder.getId());
                log.warn("Failed to send Item ID {} to MES. Status -> FAILED_TO_SEND. Order ID {} will be re-evaluated for overall status.",
                        item.getId(), parentOrder.getId());
            }
        }

        updateAffectedClientOrderStatuses(affectedClientOrderIds);

        log.info("Daily Scheduling Task Finished. Total pieces scheduled and sent to MES: {}", piecesScheduledThisRun);
    }

    private void updateAffectedClientOrderStatuses(Set<Long> clientOrderIds) {
        if (clientOrderIds.isEmpty()) {
            return;
        }
        log.debug("Re-evaluating status for ClientOrder IDs: {}", clientOrderIds);

        for (Long orderId : clientOrderIds) {
            ClientOrder order = clientOrderRepository.findById(orderId).orElse(null);
            if (order == null) {
                log.warn("Could not find ClientOrder with ID {} for status update.", orderId);
                continue;
            }

            List<OrderItem> itemsOfThisOrder = orderItemRepository.findByClientOrder_Id(orderId);
            if (itemsOfThisOrder.isEmpty() && order.getStatus() == OrderStatus.PENDING) {
                log.warn("Order ID {} is PENDING but has no items. Setting to an error or re-evaluating logic.", order.getId());
                continue;
            }


            boolean allItemsSentOrBeyond = true;
            boolean anyItemFailedToSend = false;

            for (OrderItem item : itemsOfThisOrder) {
                if (item.getStatus() == OrderItemStatus.PENDING) {
                    allItemsSentOrBeyond = false;
                    break;
                }
                if (item.getStatus() == OrderItemStatus.FAILED_TO_SEND) {
                    anyItemFailedToSend = true;
                    allItemsSentOrBeyond = false;
                    break;
                }
            }

            if (allItemsSentOrBeyond && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.SENT_TO_MES);
                clientOrderRepository.save(order);
                log.info("Updated ClientOrder ID {} status to SENT_TO_MES.", order.getId());
            } else if (anyItemFailedToSend && order.getStatus() == OrderStatus.PENDING) {
                log.info("ClientOrder ID {} remains PENDING due to FAILED_TO_SEND items.", order.getId());
            } else if (!allItemsSentOrBeyond && order.getStatus() == OrderStatus.PENDING) {
                log.info("ClientOrder ID {} remains PENDING as not all items are sent/processed yet.", order.getId());
            }
        }
    }
}