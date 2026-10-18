package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.SimpleDomainEventPublisher;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final SimpleDomainEventPublisher simpleDomainEventPublisher;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository, SimpleDomainEventPublisher simpleDomainEventPublisher) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
        this.simpleDomainEventPublisher = simpleDomainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courier = courierRepository.getById(command.getCourierId());
        if (courier.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }

        var order = orderRepository.getById(command.getOrderId());
        if (order.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("order", command.getOrderId()));
        }

        var completeAssignmentResult = courier.get().completeOrder(command.getOrderId());
        if (completeAssignmentResult.isFailure()) {
            return UnitResult.failure(completeAssignmentResult.getError());
        }

        var completeOrderResult = order.get().complete();
        if (completeOrderResult.isFailure()) {
            return UnitResult.failure(completeOrderResult.getError());
        }

        courierRepository.update(courier.get());
        orderRepository.update(order.get());
        simpleDomainEventPublisher.publish(List.of(order.get()));
        return UnitResult.success();
    }
}
