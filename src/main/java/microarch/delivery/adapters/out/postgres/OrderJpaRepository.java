package microarch.delivery.adapters.out.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findFirstByOrderStatus(OrderStatus orderStatus);

    List<Order> findAllByOrderStatus(OrderStatus orderStatus);

    List<Order> findAllByOrderStatusOrOrderStatus(OrderStatus first, OrderStatus second);
}
