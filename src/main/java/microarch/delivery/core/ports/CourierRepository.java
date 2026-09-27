package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CourierRepository extends JpaRepository<Courier, UUID> {
}
