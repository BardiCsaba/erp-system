package pt.feup.industrial.erpsystem.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.feup.industrial.erpsystem.dto.OrderItemCompletionDto;
import pt.feup.industrial.erpsystem.service.OrderService;

@RestController
@RequestMapping("/api/erp/order-items")
public class OrderCompletionController {

    private static final Logger log = LoggerFactory.getLogger(OrderCompletionController.class);
    private final OrderService orderService;

    @Autowired
    public OrderCompletionController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PutMapping("/{erpOrderItemId}/complete")
    public ResponseEntity<Void> receiveCompletionNotification(
            @PathVariable Long erpOrderItemId,
            @Valid @RequestBody OrderItemCompletionDto completionDto) {

        if (!erpOrderItemId.equals(completionDto.getErpOrderItemId())) {
            log.warn("Path variable erpOrderItemId ({}) does not match ID in request body ({}). Using ID from body.",
                    erpOrderItemId, completionDto.getErpOrderItemId());
        }


        log.info("Received completion notification via REST for ERP Order Item ID: {}", completionDto.getErpOrderItemId());

        boolean success = orderService.markOrderItemAsCompleted(completionDto);

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}