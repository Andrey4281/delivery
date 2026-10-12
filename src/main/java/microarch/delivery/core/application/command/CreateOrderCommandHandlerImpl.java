package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final GeoClient geoClient;
    private final Random random = new Random();

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, GeoClient geoClient) {
        this.orderRepository = orderRepository;
        this.geoClient = geoClient;
    }

    @Override
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var location = geoClient.getLocation(command.getAddress());
        if (location.isFailure()) {
            return UnitResult.failure(location.getError());
        }
        var orderResult = Order.create(command.getOrderID(), location.getValue(), command.getVolume());
        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }
        orderRepository.add(orderResult.getValue());
        return UnitResult.success();
    }
}
