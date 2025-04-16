package pt.feup.industrial.erpsystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clients", uniqueConstraints = {
        @UniqueConstraint(name = "uk_client_nif", columnNames = {"nif"})
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "clientOrders")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "Client name cannot be blank")
    @Size(max = 255, message = "Client name cannot exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull(message = "Client NIF cannot be null")
    @Column(name = "nif", nullable = false, unique = true)
    private Long nif;

    @OneToMany(
            mappedBy = "client",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private List<ClientOrder> clientOrders = new ArrayList<>();

    public void addClientOrder(ClientOrder order) {
        clientOrders.add(order);
        order.setClient(this);
    }

    public void removeClientOrder(ClientOrder order) {
        clientOrders.remove(order);
        order.setClient(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        if (id != null && client.id != null) {
            return Objects.equals(id, client.id);
        }
        return Objects.equals(nif, client.nif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : nif);
    }
}
