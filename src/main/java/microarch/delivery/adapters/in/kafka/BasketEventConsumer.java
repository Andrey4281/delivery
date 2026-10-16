package microarch.delivery.adapters.in.kafka;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.command.CreateOrderCommand;
import microarch.delivery.core.application.command.CreateOrderCommandHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import queues.basket.events.BasketEventsProto;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasketEventConsumer {
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @KafkaListener(topics = "${app.kafka.basket-events-topic}")
    public void listen(byte[] message) {
        try {
            var event = BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);
            BasketEventsProto.Address address = event.getAddress();
            var createOrderCommandResult = CreateOrderCommand.create(UUID.fromString(event.getBasketId()), address.getCountry(), address.getCity(), address.getStreet(),
                    address.getHouse(), address.getApartment(), event.getVolume());
            if (createOrderCommandResult.isFailure()) {
                log.error("Failed to create CreateOrderCommand: {}", createOrderCommandResult.getError());
                return;
            }

            var result = createOrderCommandHandler.handle(createOrderCommandResult.getValue());
            if (result.isFailure()) {
                log.error("Failed to handle CreateOrderCommand: {}", result.getError());
            }
        } catch (Exception e) {
            log.error("Failed to process BasketConfirmedIntegrationEvent", e);
        }
    }
}
