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
import pt.feup.industrial.erpsystem.model.OrderStatus;
import pt.feup.industrial.erpsystem.repository.ClientOrderRepository;

import java.util.List;

@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final ClientOrderRepository clientOrderRepository;
    private final MesClientService mesClientService;

    @Autowired
    public SchedulingService(ClientOrderRepository clientOrderRepository, MesClientService mesClientService) {
        this.clientOrderRepository = clientOrderRepository;
        this.mesClientService = mesClientService;
    }

    @Scheduled(cron = "${erp.scheduling.mes-sync-cron:0 0 9 * * *}") // Default: run at 9 AM every day
    @Transactional
    public void sendPendingOrdersToMes() {
        log.info("Scheduled Task: Checking for pending orders to send to MES...");

        List<ClientOrder> pendingOrders = clientOrderRepository.findByStatus(OrderStatus.PENDING);

        if (pendingOrders.isEmpty()) {
            log.info("Scheduled Task: No pending orders found.");
            return;
        }

        log.info("Scheduled Task: Found {} pending order(s). Processing...", pendingOrders.size());
        int successfulSends = 0;
        int failedSends = 0;

        for (ClientOrder order : pendingOrders) {
            log.debug("Processing PENDING Order ID: {}", order.getId());
            boolean orderFullySent = true;

            for (OrderItem item : order.getItems()) {

                MesProductionOrderDto mesRequest = new MesProductionOrderDto(
                        order.getId(),
                        item.getId(),
                        item.getProductType(),
                        item.getQuantity(),
                        item.getDueDate()
                );

                boolean sentSuccessfully = mesClientService.sendProductionOrder(mesRequest);

                if (!sentSuccessfully) {
                    log.warn("Scheduled Task: Failed to send item ID {} (Order ID {}) to MES.", item.getId(), order.getId());
                    orderFullySent = false;
                    failedSends++;
                } else {
                    successfulSends++;
                }
            }

            if (orderFullySent) {
                log.info("Scheduled Task: Successfully sent all items for Order ID {}. Updating status to SENT_TO_MES.", order.getId());
                order.setStatus(OrderStatus.SENT_TO_MES);
                clientOrderRepository.save(order);
            } else {
                log.warn("Scheduled Task: Failed to send one or more items for Order ID {}. Status remains PENDING.", order.getId());
            }

        }

        log.info("Scheduled Task Finished: Attempted sends: {}, Successful: {}, Failed: {}",
                successfulSends + failedSends, successfulSends, failedSends);
    }
}