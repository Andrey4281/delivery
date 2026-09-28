package microarch.delivery.adapters.out.postgres;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

@Repository
public class CourierRepositoryImpl implements CourierRepository {
    private final CourierJpaRepository courierJpaRepository;

    public CourierRepositoryImpl(CourierJpaRepository courierJpaRepository) {
        this.courierJpaRepository = courierJpaRepository;
    }

    @Override
    public Courier add(Courier courier) {
        return courierJpaRepository.save(courier);
    }

    @Override
    public void update(Courier courier) {
        courierJpaRepository.save(courier);
    }

    @Override
    public Optional<Courier> getById(UUID id) {
        return courierJpaRepository.findById(id);
    }

    @Override
    public List<Courier> getAll() {
        return courierJpaRepository.findAll();
    }
}
