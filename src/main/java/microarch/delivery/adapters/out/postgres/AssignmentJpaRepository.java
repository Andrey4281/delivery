package microarch.delivery.adapters.out.postgres;

import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentJpaRepository extends JpaRepository<Assignment, UUID> {
}
