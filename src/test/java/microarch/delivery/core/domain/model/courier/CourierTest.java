package microarch.delivery.core.domain.model.courier;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Courier")
class CourierTest {

    private static Location location(int x, int y) {
        return Location.create(x, y).getValue();
    }

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    private static Order order(Location location, int volume) {
        return Order.create(UUID.randomUUID(), location, volume(volume)).getValue();
    }

    private static Courier courier(Location location) {
        return Courier.create("Иван", location).getValue();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт курьера с MaxVolume = 20 и пустыми назначениями")
        void createsCourierWithMaxVolume20() {
            var location = location(5, 5);

            var result = Courier.create("Иван", location);

            assertThat(result.isSuccess()).isTrue();
            var courier = result.getValue();
            assertThat(courier.getName()).isEqualTo("Иван");
            assertThat(courier.getLocation()).isEqualTo(location);
            assertThat(courier.getMaxVolume()).isEqualTo(volume(20));
            assertThat(courier.getAssignments()).isEmpty();
        }

        @Test
        @DisplayName("бросает NPE при null-аргументах (fail-fast)")
        void throwsOnNullArguments() {
            assertThatThrownBy(() -> Courier.create(null, location(5, 5))).isInstanceOf(NullPointerException.class).hasMessageContaining("name");
            assertThatThrownBy(() -> Courier.create("Иван", null)).isInstanceOf(NullPointerException.class).hasMessageContaining("location");
        }
    }

    @Nested
    @DisplayName("takeOrder")
    class TakeOrder {

        @Test
        @DisplayName("создаёт Assignment с ID заказа; статус заказа не меняется")
        void takesOrderWithinMaxVolume() {
            var courier = courier(location(5, 5));
            var order = order(location(6, 5), 15);

            var result = courier.takeOrder(order);

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getAssignments()).hasSize(1);
            assertThat(courier.getAssignments().get(0).getOrderId()).isEqualTo(order.getId());
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("отказывает при повторном взятии того же заказа")
        void rejectsTakingSameOrderTwice() {
            var courier = courier(location(5, 5));
            var order = order(location(6, 5), 5);
            courier.takeOrder(order);

            var result = courier.takeOrder(order);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.orderIsAlreadyAssigned());
            assertThat(courier.getAssignments()).hasSize(1);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("отказывает, если сумма объёмов с новым заказом превышает 20, заказ остаётся CREATED")
        void rejectsOrderExceedingMaxVolume() {
            var courier = courier(location(5, 5));
            courier.takeOrder(order(location(6, 5), 15));
            var order = order(location(5, 6), 6);

            var result = courier.takeOrder(order);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.maximumOrderVolumeForTheCourierExceeded());
            assertThat(courier.getAssignments()).hasSize(1);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("не учитывает объём завершённых назначений")
        void ignoresVolumeOfCompletedAssignments() {
            var courier = courier(location(5, 5));
            var first = order(location(5, 5), 20);
            courier.takeOrder(first);
            courier.completeOrder(first);

            var result = courier.takeOrder(order(location(5, 5), 20));

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("completeOrder")
    class CompleteOrder {

        @Test
        @DisplayName("завершает назначение на дистанции 1 или ближе; статус заказа не меняется")
        void completesAssignmentWithinDistanceOne() {
            var courier = courier(location(5, 5));
            var order = order(location(6, 5), 5);
            courier.takeOrder(order);

            var result = courier.completeOrder(order);

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("отказывает, если курьер дальше 1 клетки от заказа; назначение остаётся ASSIGNED")
        void rejectsWhenTooFarFromOrder() {
            var courier = courier(location(5, 5));
            var order = order(location(8, 5), 5);
            courier.takeOrder(order);

            var result = courier.completeOrder(order);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.courierMustHaveRightDistanceToOrder());
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("отказывает, если заказ не назначен курьеру")
        void rejectsWhenOrderIsNotAssigned() {
            var courier = courier(location(5, 5));

            var result = courier.completeOrder(order(location(5, 5), 5));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.orderDoesNotExist());
        }

        @Test
        @DisplayName("отказывает при повторном завершении того же заказа")
        void rejectsWhenAssignmentAlreadyCompleted() {
            var courier = courier(location(5, 5));
            var order = order(location(5, 5), 5);
            courier.takeOrder(order);
            courier.completeOrder(order);

            var result = courier.completeOrder(order);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.assignmentIsAlreadyCompleted());
        }
    }

    @Nested
    @DisplayName("move")
    class Move {

        @Test
        @DisplayName("перемещает курьера на 1 шаг")
        void movesToAdjacentLocation() {
            var courier = courier(location(5, 5));

            var result = courier.move(location(6, 5));

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getLocation()).isEqualTo(location(6, 5));
        }

        @Test
        @DisplayName("отказывает при перемещении дальше 1 шага")
        void rejectsMoveFartherThanOneStep() {
            var courier = courier(location(5, 5));

            var result = courier.move(location(7, 5));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.theCourierCanMoveOnlyToAnAdjacentCell());
            assertThat(courier.getLocation()).isEqualTo(location(5, 5));
        }
    }
}
