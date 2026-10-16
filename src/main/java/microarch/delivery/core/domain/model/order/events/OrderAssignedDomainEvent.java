package microarch.delivery.core.domain.model.order.events;

import libs.ddd.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import java.util.UUID;

@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
public class OrderAssignedDomainEvent extends DomainEvent {
    private final UUID orderId;

    public OrderAssignedDomainEvent(Order order) {
        super();
        this.orderId = order.getId();
    }
}
