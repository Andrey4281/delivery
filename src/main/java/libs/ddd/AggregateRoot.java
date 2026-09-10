package libs.ddd;

import java.util.List;

public interface AggregateRoot<IdT> {
    IdT getId();

    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
