package microarch.delivery.core.domain.model.order;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.adapters.out.postgres.OrderStatusConverter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Order extends Aggregate<UUID> {
    @Embedded
    private final Location location;
    @Embedded
    private final Volume volume;
    @Column(name = "order_status")
    @Convert(converter = OrderStatusConverter.class)
    private OrderStatus orderStatus;

    private Order(UUID id, Location location, Volume volume, OrderStatus orderStatus) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.orderStatus = orderStatus;
    }

    public static Result<Order, Error> create(UUID id, Location location, Volume volume) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(volume, "volume");

        return Result.success(new Order(id, location, volume, OrderStatus.CREATED));
    }

    public UnitResult<Error> assign() {
        if (orderStatus != OrderStatus.CREATED) {
            return UnitResult.failure(Errors.mustBeCreatedToAssign());
        }
        orderStatus = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (orderStatus != OrderStatus.ASSIGNED) {
            return UnitResult.failure(Errors.mustBeAssignedToComplete());
        }
        orderStatus = OrderStatus.COMPLETED;
        return UnitResult.success();
    }

    public boolean isCreated() {
        return OrderStatus.CREATED.equals(orderStatus);
    }

    public static class Errors {
        public static Error mustBeCreatedToAssign() {
            return Error.of("order.must.be.created.to.assign", "The order must have the Created status to be assigned.");
        }

        public static Error mustBeAssignedToComplete() {
            return Error.of("order.must.be.assigned.to.complete", "The order must have the Assigned status to be completed.");
        }
    }
}
