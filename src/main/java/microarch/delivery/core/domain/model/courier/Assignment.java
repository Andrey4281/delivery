package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import java.util.Objects;
import java.util.UUID;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.adapters.out.postgres.AssignmentStatusConverter;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public final class Assignment extends BaseEntity<UUID> {
    @Column(name = "order_id")
    private final UUID orderId;
    @Embedded
    private final Location location;
    @Embedded
    private final Volume volume;
    @Column(name = "assignment_status")
    @Convert(converter = AssignmentStatusConverter.class)
    private AssignmentStatus status;

    private Assignment(UUID orderId, Location location, Volume volume, AssignmentStatus status) {
        super(UUID.randomUUID());
        this.orderId = orderId;
        this.location = location;
        this.volume = volume;
        this.status = status;
    }

    public static Result<Assignment, Error> create(UUID orderId, Location location, Volume volume) {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(volume, "volume");

        return Result.success(new Assignment(orderId, location, volume, AssignmentStatus.ASSIGNED));
    }

    // сделал метод смены статуса со scope - внутри пакета с агрегатом courier, чтобы не могли изменить статус извне
    void completeAssignment() {
        this.status = AssignmentStatus.COMPLETED;
    }

    boolean isAssigned() {
        return AssignmentStatus.ASSIGNED.equals(status);
    }

    boolean isCompleted() {
        return AssignmentStatus.COMPLETED.equals(status);
    }

    public static class Errors {

    }
}
