package pt.feup.industrial.erpsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.erpsystem.model.ClientOrder;
import java.util.Optional;

public interface ClientOrderRepository extends JpaRepository<ClientOrder, Long> {
    Optional<ClientOrder> findByClient_IdAndClientOrderId(Long clientId, Long clientOrderId);
}
