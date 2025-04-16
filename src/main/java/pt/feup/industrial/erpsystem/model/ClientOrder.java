package pt.feup.industrial.erpsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "client_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_client_clientorderid", columnNames = {"client_id", "client_order_id"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"client", "items"})
public class ClientOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Client-provided OrderID cannot be null")
    @Column(name = "client_order_id", nullable = false)
    private Long clientOrderId;

    @NotNull(message = "Received timestamp cannot be null")
    @Column(name = "received_timestamp", nullable = false, updatable = false)
    private LocalDateTime receivedTimestamp;

    @NotNull(message = "ClientOrder must be associated with a Client")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clientorder_client"))
    @JsonBackReference
    private Client client;

    @NotNull(message = "Order status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(
            mappedBy = "clientOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        receivedTimestamp = LocalDateTime.now();
    }

    public void addOrderItem(OrderItem item) {
        items.add(item);
        item.setClientOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        items.remove(item);
        item.setClientOrder(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientOrder that = (ClientOrder) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(client, that.client) && Objects.equals(clientOrderId, that.clientOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : Objects.hash(client, clientOrderId));
    }
}