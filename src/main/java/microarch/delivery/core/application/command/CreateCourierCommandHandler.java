package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface CreateCourierCommandHandler {
    UnitResult<Error> handle(CreateCourierCommand command);
}
