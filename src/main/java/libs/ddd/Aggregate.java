package libs.ddd;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class Aggregate<IdT extends Comparable<IdT>> extends BaseEntity<IdT> implements AggregateRoot<IdT> {
    @Transient
    protected List<DomainEvent> domainEvents = new ArrayList<>();

    protected Aggregate() {
        this.domainEvents = new ArrayList<>();
    }

    protected Aggregate(IdT id) {
        super(id);
        this.domainEvents = new ArrayList<>();
    }

    public List<DomainEvent> getDomainEvents() {
        return List.copyOf(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public void raiseDomainEvent(DomainEvent domainEvent) {
        if (domainEvents == null) {
            domainEvents = new ArrayList<>();
        }
        domainEvents.add(domainEvent);
    }
}
