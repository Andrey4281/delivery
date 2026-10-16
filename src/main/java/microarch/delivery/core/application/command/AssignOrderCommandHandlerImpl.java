package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.SimpleDomainEventPublisher;
import microarch.delivery.core.domain.services.OrderService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderService orderService;
    private final SimpleDomainEventPublisher simpleDomainEventPublisher;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository, OrderService orderService,
            SimpleDomainEventPublisher simpleDomainEventPublisher) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.orderService = orderService;
        this.simpleDomainEventPublisher = simpleDomainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var order = orderRepository.getFirstInCreatedStatus();
        if (order.isEmpty()) {
            return UnitResult.success();
        }

        var couriers = courierRepository.getAll();
        var assignResult = orderService.assignOrder(order.get(), couriers);
        if (assignResult.isFailure()) {
            return UnitResult.failure(assignResult.getError());
        }

        orderRepository.update(order.get());
        courierRepository.update(assignResult.getValue());
        simpleDomainEventPublisher.publish(List.of(order.get()));
        return UnitResult.success();
    }
}
