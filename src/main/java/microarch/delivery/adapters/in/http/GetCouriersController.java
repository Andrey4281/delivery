package microarch.delivery.adapters.in.http;

import java.util.List;
import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.GetCouriersApi;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GetCouriersController implements GetCouriersApi {
    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {
        var couriers = getAllCouriersQueryHandler.handle().stream()
                .map(courierDto -> new Courier(courierDto.courierId(), courierDto.courierName(), new Location(courierDto.x(), courierDto.y()))).toList();
        return ResponseEntity.ok(couriers);
    }
}
