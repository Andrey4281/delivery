package microarch.delivery.core.domain.model;

import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Location extends ValueObject<Location> {
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 10;
    private final int x;
    private final int y;

    public static Result<Location, Error> create(int x, int y) {
        var error = Guard.combine(Guard.againstLessThan(x, MIN_VALUE, "x"), Guard.againstGreaterThan(x, MAX_VALUE, "x"), Guard.againstLessThan(y, MIN_VALUE, "y"),
                Guard.againstGreaterThan(y, MAX_VALUE, "y"));
        if (error != null) {
            return Result.failure(error);
        }

        return Result.success(new Location(x, y));
    }

    public int distance(Location other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(x, y);
    }
}
