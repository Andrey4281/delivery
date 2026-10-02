package microarch.delivery.core.application.command;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {
    private final CourierRepository courierRepository;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CreateCourierCommand command) {
        var locationResult = Location.create(1, 1);
        if (locationResult.isFailure()) {
            return UnitResult.failure(locationResult.getError());
        }

        var courierResult = Courier.create(command.getName(), locationResult.getValue());
        if (courierResult.isFailure()) {
            return UnitResult.failure(courierResult.getError());
        }

        courierRepository.add(courierResult.getValue());
        return UnitResult.success();
    }
}
