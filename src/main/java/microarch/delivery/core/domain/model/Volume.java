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
public final class Volume extends ValueObject<Volume> {
    private static final int MIN_VALUE = 1;

    private final int value;

    public static Result<Volume, Error> create(int value) {
        var err = Guard.againstLessThan(value, MIN_VALUE, "value");
        if (err != null)
            return Result.failure(err);

        return Result.success(new Volume(value));
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(value);
    }

    public boolean isLessThan(Volume other) {
        return this.compareTo(other) < 0;
    }

    public boolean isGreaterThan(Volume other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessOrEqual(Volume other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isGreaterOrEqual(Volume other) {
        return this.compareTo(other) >= 0;
    }
}
