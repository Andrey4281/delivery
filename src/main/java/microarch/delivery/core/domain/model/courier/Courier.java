package microarch.delivery.core.domain.model.courier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Courier extends Aggregate<UUID> {
    private final String name;
    private final Location location;
    private final Volume maxVolume = Volume.create(20).getValue();
    private final List<Assignment> assignments = new ArrayList<>();

    private Courier(String name, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
    }

    public UnitResult<Error> takeOrder(Order order) {
        Objects.requireNonNull(order, "order");
        if (canTakeOrder(order)) {
            assignments.add(Assignment.create(order.getId(), order.getLocation(), order.getVolume()).getValue());
            return UnitResult.success();
        } else {
            return UnitResult.failure(Errors.maximumOrderVolumeForTheCourierExceeded());
        }
    }

    private boolean canTakeOrder(Order order) {
        Volume requiredVolume = assignments.stream()
            .filter(Assignment::isAssigned)
            .map(Assignment::getVolume)
            .reduce(Volume::add)
            .map(v -> v.add(order.getVolume()))
            .orElse(order.getVolume());
        return requiredVolume.isLessOrEqual(maxVolume);
    }

    public UnitResult<Error> completeOrder(Order order) {
        return assignments.stream().filter(a -> a.getOrderId().equals(order.getId()))
            .findFirst().map(assignment -> {
                if (location.distance(order.getLocation()) <= 1) {
                    assignment.completeAssignment();
                    return UnitResult.success();
                } else {
                    return UnitResult.failure(Errors.courierMustHaveRightDistanceToOrder());
                }
            })
            .orElseGet(() -> UnitResult.failure(Errors.orderDoesNotExist()));
    }

    public static class Errors {
        public static Error maximumOrderVolumeForTheCourierExceeded() {
            return Error.of("maximum.order.volume.for.the.courier.exceeded", "Maximum order volume for the courier exceeded.");
        }

        public static Error courierMustHaveRightDistanceToOrder() {
            return Error.of("courier.must.have.right.distance.to.order", "The courier must be at a distance of 1 or located within the order grid cell.");
        }

        public static Error orderDoesNotExist() {
            return Error.of("order.does.not.exist", "Order does not exist");
        }
    }
}
