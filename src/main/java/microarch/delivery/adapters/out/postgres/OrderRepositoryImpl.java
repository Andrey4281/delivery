package microarch.delivery.adapters.out.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order add(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public void update(Order order) {
        orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> getById(UUID id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Optional<Order> getFirstInCreatedStatus() {
        return orderJpaRepository.findFirstByOrderStatus(OrderStatus.CREATED);
    }

    @Override
    public List<Order> getAllInAssignedStatus() {
        return orderJpaRepository.findAllByOrderStatus(OrderStatus.ASSIGNED);
    }
}
