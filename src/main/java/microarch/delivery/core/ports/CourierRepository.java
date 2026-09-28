package microarch.delivery.core.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import microarch.delivery.core.domain.model.courier.Courier;

public interface CourierRepository {

    /**
     * Добавляет нового курьера (без назначений).
     */
    Courier add(Courier courier);

    /**
     * Обновляет ранее сохранённого курьера, включая список его назначений.
     */
    void update(Courier courier);

    /**
     * Получает курьера по идентификатору вместе со списком его назначений.
     */
    Optional<Courier> getById(UUID id);

    /**
     * Получает всех курьеров вместе со списками их назначений.
     */
    List<Courier> getAll();
}
