package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CompleteOrderApi;
import microarch.delivery.core.application.command.CompleteOrderCommand;
import microarch.delivery.core.application.command.CompleteOrderCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static microarch.delivery.adapters.in.http.ErrorResponses.badRequest;

@RestController
@RequiredArgsConstructor
public class CompleteOrderController implements CompleteOrderApi {
    private final CompleteOrderCommandHandler completeOrderCommandHandler;

    @Override
    public ResponseEntity<Void> completeOrder(UUID courierId, UUID orderId) {
        var commandResult = CompleteOrderCommand.create(courierId, orderId);
        if (commandResult.isFailure()) {
            return badRequest(commandResult.getError());
        }

        var result = completeOrderCommandHandler.handle(commandResult.getValue());
        if (result.isFailure()) {
            return badRequest(result.getError());
        }

        return ResponseEntity.ok().build();
    }
}
