package microarch.delivery.core.domain.model.courier;

import java.util.Objects;
import java.util.UUID;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Assignment extends BaseEntity<UUID> {
    private final UUID orderId;
    private final Location location;
    private final Volume volume;
    private AssignmentStatus status;

    public static Result<Assignment, Error> create(UUID orderId, Location location, Volume volume) {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(volume, "volume");

        return Result.success(new Assignment(orderId, location, volume, AssignmentStatus.ASSIGNED));
    }

    public UnitResult<Error> completeAssignment(Location courierLocation) {
        if (location.distance(courierLocation) <= 1) {
            status = AssignmentStatus.COMPLETED;
            return UnitResult.success();
        } else {
            return UnitResult.failure(Errors.courierMustHaveRightDistanceToOrder());
        }
    }

    public static class Errors {
        public static Error courierMustHaveRightDistanceToOrder() {
            return Error.of("courier.must.have.right.distance.to.order", "The courier must be at a distance of 1 or located within the order grid cell.");
        }
    }
}
