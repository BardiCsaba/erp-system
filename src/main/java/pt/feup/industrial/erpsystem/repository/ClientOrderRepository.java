package pt.feup.industrial.erpsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import pt.feup.industrial.erpsystem.model.Client;
import pt.feup.industrial.erpsystem.model.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface ClientOrderRepository extends JpaRepository<ClientOrder, Long> {

    Optional<ClientOrder> findByClient_IdAndClientOrderId(Long clientId, Long clientOrderId);

    List<ClientOrder> findByClient(Client client);

    List<ClientOrder> findByClient_Id(Long clientId);

    List<ClientOrder> findByClient_Nif(Long nif);

    List<ClientOrder> findByClientOrderId(Long clientOrderId);

    List<ClientOrder> findByStatus(OrderStatus status);
}
