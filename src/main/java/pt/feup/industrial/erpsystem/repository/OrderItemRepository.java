package pt.feup.industrial.erpsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pt.feup.industrial.erpsystem.model.OrderItem;
import pt.feup.industrial.erpsystem.model.OrderItemStatus;
import pt.feup.industrial.erpsystem.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByDueDate(LocalDate dueDate);
    List<OrderItem> findByProductType(Integer productType);
    List<OrderItem> findByDueDateLessThanEqual(LocalDate dueDate);
    List<OrderItem> findByProductTypeAndDueDate(Integer productType, LocalDate dueDate);
    List<OrderItem> findByClientOrder_Id(Long clientOrderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.status = :itemStatus AND oi.clientOrder.status = :orderStatus ORDER BY oi.dueDate ASC, oi.clientOrder.id ASC")
    List<OrderItem> findPendingItemsSortedByDueDate(OrderItemStatus itemStatus, OrderStatus orderStatus);
}