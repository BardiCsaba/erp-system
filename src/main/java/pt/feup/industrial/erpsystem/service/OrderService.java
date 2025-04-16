package pt.feup.industrial.erpsystem.service;

import pt.feup.industrial.erpsystem.dto.ClientOrderRequestDto;
import pt.feup.industrial.erpsystem.dto.OrderItemCompletionDto;
import pt.feup.industrial.erpsystem.model.Client;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import pt.feup.industrial.erpsystem.model.OrderItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    void processAndSaveOrder(ClientOrderRequestDto orderRequest);

    boolean markOrderItemAsCompleted(OrderItemCompletionDto completionDto);

    List<ClientOrder> getAllOrders();

    Optional<ClientOrder> getOrderById(Long internalOrderId);

    List<ClientOrder> getOrdersByClientNif(Long nif);

    List<OrderItem> getItemsDueOn(LocalDate date);

    List<OrderItem> getItemsByType(Integer productType);

    Optional<Client> getClientByNif(Long nif);

}
