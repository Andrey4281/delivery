package microarch.delivery.core.domain.services;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderServiceImpl")
class OrderServiceImplTest {

    private final OrderService service = new OrderServiceImpl();

    private static Location location(int x, int y) {
        return Location.create(x, y).getValue();
    }

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    private static Order order() {
        return Order.create(UUID.randomUUID(), location(5, 5), volume(3)).getValue();
    }

    private static Courier courierAt(Location location) {
        return Courier.create("Иван", location).getValue();
    }

    @Nested
    @DisplayName("assignOrder")
    class AssignOrder {

        @Test
        @DisplayName("назначает заказ ближайшему курьеру, а не последнему в списке (регрессия компаратора)")
        void assignsOrderToClosestCourier() {
            var order = order();
            var close = courierAt(location(6, 5));
            var far = courierAt(location(1, 1));

            var result = service.assignOrder(order, List.of(close, far));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue()).isSameAs(close);
        }

        @Test
        @DisplayName("курьер берёт заказ, а заказ переходит в ASSIGNED")
        void courierTakesOrderAndOrderBecomesAssigned() {
            var order = order();
            var courier = courierAt(location(6, 5));

            var result = service.assignOrder(order, List.of(courier));

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getAssignments()).hasSize(1);
            assertThat(courier.getAssignments().get(0).getOrderId()).isEqualTo(order.getId());
            assertThat(courier.getAssignments().get(0).getLocation()).isEqualTo(order.getLocation());
            assertThat(courier.getAssignments().get(0).getVolume()).isEqualTo(order.getVolume());
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("возвращает ошибку, если заказ не в статусе CREATED; курьер не трогается")
        void failsWhenOrderIsNotCreated() {
            var order = order();
            order.assign();
            var courier = courierAt(location(5, 5));

            var result = service.assignOrder(order, List.of(courier));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(OrderServiceImpl.Errors.cantAssignIfOrderStatusIsNotCreated());
            assertThat(courier.getAssignments()).isEmpty();
        }

        @Test
        @DisplayName("возвращает ошибку из статуса COMPLETED")
        void failsWhenOrderIsCompleted() {
            var order = order();
            order.assign();
            order.complete();

            var result = service.assignOrder(order, List.of(courierAt(location(5, 5))));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(OrderServiceImpl.Errors.cantAssignIfOrderStatusIsNotCreated());
        }

        @Test
        @DisplayName("возвращает ошибку при пустом списке курьеров")
        void failsWhenCourierListIsEmpty() {
            var order = order();

            var result = service.assignOrder(order, List.of());

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(OrderServiceImpl.Errors.allCouriersAreFullyBookedOrUnavailable());
        }

        @Test
        @DisplayName("возвращает ошибку, если все курьеры загружены по объёму")
        void failsWhenAllCouriersAreFullyBooked() {
            var order = order();
            var first = courierAt(location(5, 5));
            first.takeOrder(UUID.randomUUID(), location(5, 5), volume(20));
            var second = courierAt(location(6, 5));
            second.takeOrder(UUID.randomUUID(), location(6, 5), volume(20));

            var result = service.assignOrder(order, List.of(first, second));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(OrderServiceImpl.Errors.allCouriersAreFullyBookedOrUnavailable());
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("пропускает загруженного курьера, даже если он ближе, и берёт свободного")
        void skipsFullyBookedCourierEvenIfCloser() {
            var order = order();
            var busyClose = courierAt(location(5, 6));
            busyClose.takeOrder(UUID.randomUUID(), location(5, 6), volume(20));
            var freeFar = courierAt(location(1, 5));

            var result = service.assignOrder(order, List.of(busyClose, freeFar));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue()).isSameAs(freeFar);
            assertThat(busyClose.getAssignments()).hasSize(1);
        }
    }
}
