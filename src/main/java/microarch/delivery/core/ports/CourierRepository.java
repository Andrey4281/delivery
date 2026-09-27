package microarch.delivery.core.ports;

import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, UUID> {
}
