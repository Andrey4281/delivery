package microarch.delivery.core.domain.model.order;

import libs.ddd.Aggregate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import java.util.UUID;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order extends Aggregate<UUID> {
    private final Location location;
    private final Volume volume;
    private OrderStatus orderStatus;


}
