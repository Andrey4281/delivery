package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface CreateOrderCommandHandler {
    UnitResult<Error> handle(CreateOrderCommand command);
}
