package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    @Override
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
        return UnitResult.success();
    }
}
