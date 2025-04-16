package pt.feup.industrial.erpsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Integer productType; // Example: Store P5 as 5, P6 as 6 etc.

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    private LocalDate dueDate; // Calculated: receivedDate + DDate

    @NotNull
    @PositiveOrZero
    private Double penaltyPerDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_order_id", nullable = false)
    private ClientOrder clientOrder;
}
