package microarch.delivery.core.application.command;

import java.util.List;
import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository, @Qualifier("outboxDomainEventPublisher") DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
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
        domainEventPublisher.publish(List.of(order.get()));
        return UnitResult.success();
    }
}
