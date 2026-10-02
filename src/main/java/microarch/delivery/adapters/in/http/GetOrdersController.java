package microarch.delivery.adapters.in.http;

import java.util.List;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetOrdersApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetNotCompletedOrdersQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetOrdersController implements GetOrdersApi {
    private final GetNotCompletedOrdersQueryHandler getNotCompletedOrdersQueryHandler;

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        var orders = getNotCompletedOrdersQueryHandler.handle().stream().map(orderDto -> new Order(orderDto.orderId(), new Location(orderDto.x(), orderDto.y()))).toList();
        return ResponseEntity.ok(orders);
    }
}
