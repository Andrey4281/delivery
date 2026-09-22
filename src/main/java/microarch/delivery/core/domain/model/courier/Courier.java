package microarch.delivery.core.domain.model.courier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Courier extends Aggregate<UUID> {
    @Getter
    private final String name;
    @Getter
    private Location location;
    @Getter
    private final Volume maxVolume = Volume.create(20).getValue();

    public List<Assignment> getAssignments() {
        return List.copyOf(assignments);
    }

    private final List<Assignment> assignments = new ArrayList<>();

    private Courier(String name, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.location = location;
    }

    public static Result<Courier, Error> create(String name, Location location) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(location, "location");

        return Result.success(new Courier(name, location));
    }

    public UnitResult<Error> takeOrder(UUID orderId, Location orderLocation, Volume orderVolume) {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(orderLocation, "orderLocation");
        Objects.requireNonNull(orderVolume, "orderVolume");
        if (assignments.stream().anyMatch(a -> a.getOrderId().equals(orderId))) {
            return UnitResult.failure(Errors.orderIsAlreadyAssigned());
        }
        if (canTakeOrder(orderVolume)) {
            assignments.add(Assignment.create(orderId, orderLocation, orderVolume).getValue());
            return UnitResult.success();
        } else {
            return UnitResult.failure(Errors.maximumOrderVolumeForTheCourierExceeded());
        }
    }

    public UnitResult<Error> completeOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");
        return assignments.stream().filter(a -> a.getOrderId().equals(orderId)).findFirst().map(assignment -> {
            if (assignment.isCompleted()) {
                return UnitResult.failure(Errors.assignmentIsAlreadyCompleted());
            }
            if (location.distance(assignment.getLocation()) <= 1) {
                assignment.completeAssignment();
                return UnitResult.success();
            } else {
                return UnitResult.failure(Errors.courierMustHaveRightDistanceToOrder());
            }
        }).orElseGet(() -> UnitResult.failure(Errors.orderDoesNotExist()));
    }

    public UnitResult<Error> move(Location requiredLocation) {
        Objects.requireNonNull(requiredLocation, "requiredLocation");
        if (location.distance(requiredLocation) <= 1) {
            location = requiredLocation;
            return UnitResult.success();
        } else {
            return UnitResult.failure(Errors.theCourierCanMoveOnlyToAnAdjacentCell());
        }
    }

    private boolean canTakeOrder(Volume orderVolume) {
        Volume requiredVolume = assignments.stream().filter(Assignment::isAssigned).map(Assignment::getVolume).reduce(Volume::add).map(v -> v.add(orderVolume)).orElse(orderVolume);
        return requiredVolume.isLessOrEqual(maxVolume);
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

        public static Error theCourierCanMoveOnlyToAnAdjacentCell() {
            return Error.of("the.courier.can.move.only.to.an.adjacent.cell", "The courier can move only to an adjacent cell.");
        }

        public static Error assignmentIsAlreadyCompleted() {
            return Error.of("assignment.is.already.completed", "Assignment is already completed.");
        }

        public static Error orderIsAlreadyAssigned() {
            return Error.of("order.is.already.assigned", "Order is already assigned to the courier.");
        }
    }
}
