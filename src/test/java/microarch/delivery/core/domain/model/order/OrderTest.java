package microarch.delivery.core.domain.model.order;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order")
class OrderTest {

    private static Location location(int x, int y) {
        return Location.create(x, y).getValue();
    }

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    private static Order order() {
        return Order.create(UUID.randomUUID(), location(5, 5), volume(3)).getValue();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт заказ со статусом CREATED и сохраняет все аргументы")
        void createsOrderWithCreatedStatus() {
            var id = UUID.randomUUID();
            var location = location(5, 5);
            var volume = volume(3);

            var result = Order.create(id, location, volume);

            assertThat(result.isSuccess()).isTrue();
            var order = result.getValue();
            assertThat(order.getId()).isEqualTo(id);
            assertThat(order.getLocation()).isEqualTo(location);
            assertThat(order.getVolume()).isEqualTo(volume);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("бросает NPE при null-аргументах (fail-fast)")
        void throwsOnNullArguments() {
            var id = UUID.randomUUID();
            var location = location(5, 5);
            var volume = volume(1);

            assertThatThrownBy(() -> Order.create(null, location, volume)).isInstanceOf(NullPointerException.class).hasMessageContaining("id");
            assertThatThrownBy(() -> Order.create(id, null, volume)).isInstanceOf(NullPointerException.class).hasMessageContaining("location");
            assertThatThrownBy(() -> Order.create(id, location, null)).isInstanceOf(NullPointerException.class).hasMessageContaining("volume");
        }
    }

    @Nested
    @DisplayName("assign")
    class Assign {

        @Test
        @DisplayName("переводит заказ в ASSIGNED из статуса CREATED")
        void assignsFromCreated() {
            var order = order();

            var result = order.assign();

            assertThat(result.isSuccess()).isTrue();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("возвращает ошибку при повторном назначении — статус не меняется")
        void failsWhenAlreadyAssigned() {
            var order = order();
            order.assign();

            var result = order.assign();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("order.must.be.created.to.assign");
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("возвращает ошибку из статуса COMPLETED")
        void failsWhenCompleted() {
            var order = order();
            order.assign();
            order.complete();

            var result = order.assign();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("order.must.be.created.to.assign");
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("переводит заказ в COMPLETED из статуса ASSIGNED")
        void completesFromAssigned() {
            var order = order();
            order.assign();

            var result = order.complete();

            assertThat(result.isSuccess()).isTrue();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("возвращает ошибку из статуса CREATED — сначала заказ нужно назначить")
        void failsWhenCreated() {
            var order = order();

            var result = order.complete();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("order.must.be.assigned.to.complete");
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("возвращает ошибку при повторном завершении — статус не меняется")
        void failsWhenAlreadyCompleted() {
            var order = order();
            order.assign();
            order.complete();

            var result = order.complete();

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("order.must.be.assigned.to.complete");
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("полный жизненный цикл: CREATED → ASSIGNED → COMPLETED")
        void fullLifecycle() {
            var order = order();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            order.assign();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
            order.complete();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }
    }
}
