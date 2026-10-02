package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCourierCommand {
    private String name;

    public static Result<CreateCourierCommand, Error> create(String name) {
        var error = Guard.againstNullOrEmpty(name, "name");
        if (error != null) {
            return Result.failure(error);
        }
        var command = new CreateCourierCommand();
        command.name = name;
        return Result.success(command);
    }
}
