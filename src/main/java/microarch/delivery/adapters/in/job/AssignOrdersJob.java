package microarch.delivery.adapters.in.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.command.AssignOrderCommand;
import microarch.delivery.core.application.command.AssignOrderCommandHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignOrdersJob {
    private final AssignOrderCommandHandler assignOrderCommandHandler;

    @Scheduled(fixedRate = 1000)
    public void assignOrders() {
        var commandResult = AssignOrderCommand.create();
        if (commandResult.isFailure()) {
            log.error("Failed to create AssignOrderCommand: {}", commandResult.getError());
            return;
        }

        var result = assignOrderCommandHandler.handle(commandResult.getValue());
        if (result.isFailure()) {
            log.error("Failed to assign order: {}", result.getError());
        }
    }
}
