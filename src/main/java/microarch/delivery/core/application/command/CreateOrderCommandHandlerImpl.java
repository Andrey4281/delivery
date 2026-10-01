package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public UnitResult<Error> handle(CreateOrderCommand command) {
        var location = createRandomLocation();
        var orderResult = Order.create(command.getOrderID(), location, command.getVolume());
        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }
        orderRepository.add(orderResult.getValue());
        return UnitResult.success();
    }

    private Location createRandomLocation() {
        int x = random.nextInt(10) + 1;
        int y = random.nextInt(10) + 1;
        return Location.create(x, y).getValueOrThrow();
    }
}
