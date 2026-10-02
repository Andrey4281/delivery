package microarch.delivery.core.application.queries.dto;

import java.util.List;
import microarch.delivery.adapters.out.postgres.OrderJpaRepository;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

@Service
public class GetNotCompletedOrdersQueryHandlerImpl implements GetNotCompletedOrdersQueryHandler {
    private final OrderJpaRepository orderRepository;

    public GetNotCompletedOrdersQueryHandlerImpl(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderDto> handle() {
        return orderRepository.findAllByOrderStatusOrOrderStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED).stream()
                .map(order -> new OrderDto(order.getId(), order.getLocation().getX(), order.getLocation().getY())).toList();
    }
}
