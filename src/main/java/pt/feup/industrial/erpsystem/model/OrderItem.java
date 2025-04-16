package pt.feup.industrial.erpsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "clientOrder")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Product type cannot be null")
    @Column(name = "product_type", nullable = false)
    private Integer productType;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Due date cannot be null")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate; // Calculated: receivedDate + DDate

    @NotNull(message = "Penalty cannot be null")
    @PositiveOrZero(message = "Penalty must be zero or positive")
    @Column(name = "penalty_per_day", nullable = false)
    private Double penaltyPerDay;

    @NotNull(message = "Order item status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderItemStatus status = OrderItemStatus.PENDING;

    @Column(name = "completion_timestamp")
    private LocalDateTime completionTimestamp;

    @NotNull(message = "OrderItem must be associated with a ClientOrder")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orderitem_clientorder"))
    @JsonBackReference
    private ClientOrder clientOrder;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return id != null && Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
