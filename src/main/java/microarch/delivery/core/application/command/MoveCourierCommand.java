package microarch.delivery.core.application.command;

import java.util.UUID;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.Location;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveCourierCommand {
    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierCommand, Error> create(UUID courierId, int x, int y) {
        var err = Guard.againstNullOrEmpty(courierId, "courierId");
        if (err != null) {
            return Result.failure(err);
        }

        var locationResult = Location.create(x, y);
        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        return Result.success(new MoveCourierCommand(courierId, locationResult.getValue()));
    }
}
