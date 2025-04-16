package pt.feup.industrial.erpsystem.service;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.feup.industrial.erpsystem.dto.ClientOrderRequestDto;
import pt.feup.industrial.erpsystem.dto.OrderItemDto;
import pt.feup.industrial.erpsystem.model.Client;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import pt.feup.industrial.erpsystem.model.OrderItem;
import pt.feup.industrial.erpsystem.repository.ClientOrderRepository;
import pt.feup.industrial.erpsystem.repository.ClientRepository;
import pt.feup.industrial.erpsystem.repository.OrderItemRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final ClientRepository clientRepository;
    private final ClientOrderRepository clientOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final Validator validator;

    @Autowired
    public OrderServiceImpl(ClientRepository clientRepository,
                            ClientOrderRepository clientOrderRepository,
                            OrderItemRepository orderItemRepository,
                            Validator validator) {
        this.clientRepository = clientRepository;
        this.clientOrderRepository = clientOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.validator = validator;
    }

    @Override
    @Transactional
    public void processAndSaveOrder(ClientOrderRequestDto orderRequest) {
        Set<jakarta.validation.ConstraintViolation<ClientOrderRequestDto>> violations = validator.validate(orderRequest);
        if (!violations.isEmpty()) {
            log.error("Validation failed for incoming order request: {}", violations);
            return;
            // throw new IllegalArgumentException("Invalid order request: " + violations);
        }

        for (OrderItemDto itemDto : orderRequest.getOrders()) {
            if (!isValidProductType(itemDto.getType())) {
                log.error("Invalid product type {} in order {} for client {}", itemDto.getType(), orderRequest.getOrderID(), orderRequest.getNif());
                // Handle error
                return;
            }
        }

        Client client = clientRepository.findByNif(orderRequest.getNif())
                .orElseGet(() -> {
                    Client newClient = new Client();
                    newClient.setName(orderRequest.getName());
                    newClient.setNif(orderRequest.getNif());
                    log.info("Creating new client: NIF={}", newClient.getNif());
                    return clientRepository.save(newClient);
                });

        Optional<ClientOrder> existingOrder = clientOrderRepository.findByClient_IdAndClientOrderId(client.getId(), orderRequest.getOrderID());
        if (existingOrder.isPresent()) {
            log.warn("Duplicate OrderID {} received for client NIF {}. Ignoring.", orderRequest.getOrderID(), client.getNif());
            return;
        }

        ClientOrder clientOrder = new ClientOrder();
        clientOrder.setClient(client);
        clientOrder.setClientOrderId(orderRequest.getOrderID());

        List<OrderItem> orderItems = new ArrayList<>();
        LocalDateTime receivedTime = LocalDateTime.now();
        for (OrderItemDto itemDto : orderRequest.getOrders()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductType(itemDto.getType());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPenaltyPerDay(itemDto.getPenalty());
            orderItem.setDueDate(receivedTime.toLocalDate().plusDays(itemDto.getDDate()));
            orderItem.setClientOrder(clientOrder);
            orderItems.add(orderItem);
        }
        clientOrder.setItems(orderItems);

        clientOrderRepository.save(clientOrder);
        log.info("Successfully saved order {} for client NIF {}", clientOrder.getClientOrderId(), client.getNif());
    }

    private boolean isValidProductType(Integer type) {
        return type != null; // && (type == 5 || type == 6 || type == 7 || type == 9);
    }

    public List<ClientOrder> getAllOrders() {
        log.info("Fetching all client orders");
        return clientOrderRepository.findAll();
    }

    public Optional<ClientOrder> getOrderById(Long internalOrderId) {
        log.info("Fetching order by internal ID: {}", internalOrderId);
        return clientOrderRepository.findById(internalOrderId);
    }

    public List<ClientOrder> getOrdersByClientNif(Long nif) {
        log.info("Fetching orders for client NIF: {}", nif);
        return clientOrderRepository.findByClient_Nif(nif);
    }

    public List<OrderItem> getItemsDueOn(LocalDate date) {
        log.info("Fetching order items due on: {}", date);
        return orderItemRepository.findByDueDate(date);
    }

    public List<OrderItem> getItemsByType(Integer productType) {
        log.info("Fetching order items for product type: {}", productType);
        return orderItemRepository.findByProductType(productType);
    }

    public Optional<Client> getClientByNif(Long nif) {
        log.info("Fetching client by NIF: {}", nif);
        return clientRepository.findByNif(nif);
    }
}
