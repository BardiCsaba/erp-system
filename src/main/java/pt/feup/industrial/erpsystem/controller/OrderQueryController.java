package pt.feup.industrial.erpsystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import pt.feup.industrial.erpsystem.model.Client;
import pt.feup.industrial.erpsystem.model.OrderItem;
import pt.feup.industrial.erpsystem.service.OrderService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/query")
public class OrderQueryController {

    private final OrderService orderService;

    @Autowired
    public OrderQueryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/all")
    public List<ClientOrder> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/orders/{internalId}")
    public ResponseEntity<ClientOrder> getOrderById(@PathVariable Long internalId) {
        return orderService.getOrderById(internalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/by-client-nif/{nif}")
    public List<ClientOrder> getOrdersByClientNif(@PathVariable Long nif) {
        return orderService.getOrdersByClientNif(nif);
    }

    @GetMapping("/items/by-type/{type}")
    public List<OrderItem> getItemsByType(@PathVariable Integer type) {
        return orderService.getItemsByType(type);
    }

    // Example: GET /api/query/items/due?date=YYYY-MM-DD
    @GetMapping("/items/due")
    public List<OrderItem> getItemsDue(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return orderService.getItemsDueOn(date);
    }

    @GetMapping("/clients/by-nif/{nif}")
    public ResponseEntity<Client> getClientByNif(@PathVariable Long nif) {
        return orderService.getClientByNif(nif)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
