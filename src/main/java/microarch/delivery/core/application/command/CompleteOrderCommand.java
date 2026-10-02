package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CompleteOrderCommand {
    private final UUID courierId;
    private final UUID orderId;

    public static Result<CompleteOrderCommand, Error> create(UUID courierId, UUID orderId) {
        var courierIdError = Guard.againstNullOrEmpty(courierId, "courierId");
        if (courierIdError != null) {
            return Result.failure(courierIdError);
        }

        var orderIdError = Guard.againstNullOrEmpty(orderId, "orderId");
        if (orderIdError != null) {
            return Result.failure(orderIdError);
        }

        return Result.success(new CompleteOrderCommand(courierId, orderId));
    }
}
