package microarch.delivery;

import libs.ddd.Aggregate;
import libs.ddd.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.DomainEventProducer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimpleDomainEventPublisher implements DomainEventPublisher {
    private final DomainEventProducer producer;

    @Override
    public void publish(Iterable<? extends Aggregate<?>> aggregates) {
        for (var aggregate : aggregates) {
            for (var event : aggregate.getDomainEvents()) {
                producer.produce(event);
            }
            aggregate.clearDomainEvents();
        }
    }
}
