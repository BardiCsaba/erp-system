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

import java.util.List;

@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final ClientOrderRepository clientOrderRepository;
    private final MesClientService mesClientService;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public SchedulingService(ClientOrderRepository clientOrderRepository, MesClientService mesClientService, OrderItemRepository orderItemRepository) {
        this.clientOrderRepository = clientOrderRepository;
        this.mesClientService = mesClientService;
        this.orderItemRepository = orderItemRepository;
    }

    @Scheduled(cron = "${erp.scheduling.mes-sync-cron:0 0 9 * * *}") // Default: run at 9 AM every day
    @Transactional
    public void sendPendingOrdersToMes() {
        log.info("Scheduled Task: Checking for PENDING orders to send to MES...");

        List<ClientOrder> pendingOrders = clientOrderRepository.findByStatus(OrderStatus.PENDING);

        // TODO Pending orders scheduling logic

        if (pendingOrders.isEmpty()) {
            log.info("Scheduled Task: No PENDING orders found.");
            return;
        }

        log.info("Scheduled Task: Found {} PENDING order(s). Processing...", pendingOrders.size());

        for (ClientOrder order : pendingOrders) {
            boolean orderSendAttempted = false;
            boolean anyItemSendFailed = false;
            int itemsToSend = 0;
            int itemsSentSuccessfully = 0;

            log.debug("Processing PENDING Order ID: {}", order.getId());

            ClientOrder orderInTx = clientOrderRepository.findById(order.getId()).orElse(null);
            if (orderInTx == null || orderInTx.getStatus() != OrderStatus.PENDING) {
                log.warn("Order ID {} no longer PENDING or not found, skipping.", order.getId());
                continue;
            }

            for (OrderItem item : orderInTx.getItems()) {
                if (item.getStatus() == OrderItemStatus.PENDING) {
                    orderSendAttempted = true;
                    itemsToSend++;
                    log.debug("Attempting to send item ID {} (Status: {}) for Order ID {}", item.getId(), item.getStatus(), order.getId());

                    MesProductionOrderDto mesRequest = new MesProductionOrderDto(
                            orderInTx.getId(), item.getId(), item.getProductType(), item.getQuantity(), item.getDueDate());

                    boolean sentSuccessfully = mesClientService.sendProductionOrder(mesRequest);

                    if (sentSuccessfully) {
                        item.setStatus(OrderItemStatus.SENT_TO_MES);
                        orderItemRepository.save(item);
                        itemsSentSuccessfully++;
                        log.info("Successfully sent item ID {} to MES. Status -> SENT_TO_MES.", item.getId());
                    } else {
                        item.setStatus(OrderItemStatus.FAILED_TO_SEND);
                        orderItemRepository.save(item);
                        anyItemSendFailed = true;
                        log.warn("Failed to send item ID {} to MES. Status -> FAILED_TO_SEND.", item.getId());
                    }
                }
            }

            if (orderSendAttempted) {
                if (anyItemSendFailed) {
                    log.warn("Order ID {} remains PENDING because one or more items failed to send.", orderInTx.getId());
                } else if (itemsToSend > 0 && itemsSentSuccessfully == itemsToSend) {
                    boolean allItemsAccountedFor = true;
                    for(OrderItem itemCheck : orderInTx.getItems()) {
                        if (itemCheck.getStatus() == OrderItemStatus.PENDING || itemCheck.getStatus() == OrderItemStatus.FAILED_TO_SEND) {
                            allItemsAccountedFor = false;
                            break;
                        }
                    }

                    if (allItemsAccountedFor) {
                        orderInTx.setStatus(OrderStatus.SENT_TO_MES);
                        clientOrderRepository.save(orderInTx);
                        log.info("Successfully sent all required items for Order ID {}. Order Status -> SENT_TO_MES.", orderInTx.getId());
                    } else {
                        log.warn("Order ID {} still has items needing attention (PENDING/FAILED_TO_SEND), Order status remains PENDING.", orderInTx.getId());
                    }
                } else {
                    log.debug("No items required sending for Order ID {} in this run.", orderInTx.getId());
                }
            }
        }

        log.info("Scheduled Task Finished processing PENDING orders.");
    }
}