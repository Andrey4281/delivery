package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateCourierApi;
import microarch.delivery.adapters.in.http.model.CreateCourierResponse;
import microarch.delivery.adapters.in.http.model.NewCourier;
import microarch.delivery.core.application.command.CreateCourierCommand;
import microarch.delivery.core.application.command.CreateCourierCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static microarch.delivery.adapters.in.http.ErrorResponses.badRequest;

@RestController
@RequiredArgsConstructor
public class CreateCourierController implements CreateCourierApi {
    private final CreateCourierCommandHandler createCourierCommandHandler;

    @Override
    public ResponseEntity<CreateCourierResponse> createCourier(NewCourier newCourier) {
        var commandResult = CreateCourierCommand.create(newCourier.getName());
        if (commandResult.isFailure()) {
            return badRequest(commandResult.getError());
        }

        var result = createCourierCommandHandler.handle(commandResult.getValue());
        if (result.isFailure()) {
            return badRequest(result.getError());
        }

        var response = new CreateCourierResponse(result.getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
