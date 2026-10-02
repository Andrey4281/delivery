package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssignOrderCommand {

    public static Result<AssignOrderCommand, Error> create() {
        return Result.success(new AssignOrderCommand());
    }
}
