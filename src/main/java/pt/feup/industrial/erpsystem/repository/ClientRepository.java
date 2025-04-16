package pt.feup.industrial.erpsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.erpsystem.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByNif(Long nif);
}
