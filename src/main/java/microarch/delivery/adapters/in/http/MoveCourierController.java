package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.command.MoveCourierCommand;
import microarch.delivery.core.application.command.MoveCourierCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static microarch.delivery.adapters.in.http.ErrorResponses.badRequest;

@RestController
@RequiredArgsConstructor
public class MoveCourierController implements MoveCourierApi {
    private final MoveCourierCommandHandler moveCourierCommandHandler;

    @Override
    public ResponseEntity<Void> moveCourier(UUID courierId, Location location) {
        var commandResult = MoveCourierCommand.create(courierId, location.getX(), location.getY());
        if (commandResult.isFailure()) {
            return badRequest(commandResult.getError());
        }

        var result = moveCourierCommandHandler.handle(commandResult.getValue());
        if (result.isFailure()) {
            return badRequest(result.getError());
        }

        return ResponseEntity.ok().build();
    }
}
