package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;

@Service
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {
    private final CourierRepository courierRepository;

    public MoveCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public UnitResult<Error> handle(MoveCourierCommand command) {
        var courier = courierRepository.getById(command.getCourierId());
        if (courier.isEmpty()) {
            return UnitResult.failure(GeneralErrors.notFound("courier", command.getCourierId()));
        }

        var moveResult = courier.get().move(command.getLocation());
        if (moveResult.isFailure()) {
            return UnitResult.failure(moveResult.getError());
        }

        courierRepository.update(courier.get());
        return UnitResult.success();
    }
}
