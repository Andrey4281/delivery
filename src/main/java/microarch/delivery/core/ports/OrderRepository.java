package microarch.delivery.core.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import microarch.delivery.core.domain.model.order.Order;

public interface OrderRepository {

    /**
     * Добавляет новый заказ.
     */
    Order add(Order order);

    /**
     * Обновляет ранее сохранённый заказ.
     */
    void update(Order order);

    /**
     * Получает заказ по идентификатору.
     */
    Optional<Order> getById(UUID id);

    /**
     * Получает один любой новый заказ (в статусе "Created").
     */
    Optional<Order> getFirstInCreatedStatus();

    /**
     * Получает все назначенные заказы (в статусе "Assigned").
     */
    List<Order> getAllInAssignedStatus();
}
