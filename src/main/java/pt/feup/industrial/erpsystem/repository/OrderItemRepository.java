package pt.feup.industrial.erpsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.erpsystem.model.OrderItem;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByDueDate(LocalDate dueDate);
    List<OrderItem> findByProductType(Integer productType);
    List<OrderItem> findByDueDateLessThanEqual(LocalDate dueDate);
    List<OrderItem> findByProductTypeAndDueDate(Integer productType, LocalDate dueDate);
    List<OrderItem> findByClientOrder_Id(Long clientOrderId);
}