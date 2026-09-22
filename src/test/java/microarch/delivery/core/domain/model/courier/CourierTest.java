package microarch.delivery.core.domain.model.courier;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

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
        @DisplayName("создаёт Assignment с ID заказа и переданными координатами и объёмом")
        void takesOrderWithinMaxVolume() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();

            var result = courier.takeOrder(orderId, location(6, 5), volume(15));

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getAssignments()).hasSize(1);
            assertThat(courier.getAssignments().get(0).getOrderId()).isEqualTo(orderId);
            assertThat(courier.getAssignments().get(0).getLocation()).isEqualTo(location(6, 5));
            assertThat(courier.getAssignments().get(0).getVolume()).isEqualTo(volume(15));
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("бросает NPE при null-аргументах (fail-fast)")
        void throwsOnNullArguments() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();

            assertThatThrownBy(() -> courier.takeOrder(null, location(6, 5), volume(15))).isInstanceOf(NullPointerException.class).hasMessageContaining("orderId");
            assertThatThrownBy(() -> courier.takeOrder(orderId, null, volume(15))).isInstanceOf(NullPointerException.class).hasMessageContaining("orderLocation");
            assertThatThrownBy(() -> courier.takeOrder(orderId, location(6, 5), null)).isInstanceOf(NullPointerException.class).hasMessageContaining("orderVolume");
        }

        @Test
        @DisplayName("отказывает при повторном взятии того же заказа")
        void rejectsTakingSameOrderTwice() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();
            courier.takeOrder(orderId, location(6, 5), volume(5));

            var result = courier.takeOrder(orderId, location(5, 6), volume(5));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.orderIsAlreadyAssigned());
            assertThat(courier.getAssignments()).hasSize(1);
        }

        @Test
        @DisplayName("отказывает, если сумма объёмов назначений с новым заказом превышает 20")
        void rejectsOrderExceedingMaxVolume() {
            var courier = courier(location(5, 5));
            courier.takeOrder(UUID.randomUUID(), location(6, 5), volume(15));

            var result = courier.takeOrder(UUID.randomUUID(), location(5, 6), volume(6));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.maximumOrderVolumeForTheCourierExceeded());
            assertThat(courier.getAssignments()).hasSize(1);
        }

        @Test
        @DisplayName("не учитывает объём завершённых назначений")
        void ignoresVolumeOfCompletedAssignments() {
            var courier = courier(location(5, 5));
            var first = UUID.randomUUID();
            courier.takeOrder(first, location(5, 5), volume(20));
            courier.completeOrder(first);

            var result = courier.takeOrder(UUID.randomUUID(), location(5, 5), volume(20));

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("completeOrder")
    class CompleteOrder {

        @Test
        @DisplayName("завершает назначение на дистанции 1 или ближе")
        void completesAssignmentWithinDistanceOne() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();
            courier.takeOrder(orderId, location(6, 5), volume(5));

            var result = courier.completeOrder(orderId);

            assertThat(result.isSuccess()).isTrue();
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("отказывает, если курьер дальше 1 клетки от заказа; назначение остаётся ASSIGNED")
        void rejectsWhenTooFarFromOrder() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();
            courier.takeOrder(orderId, location(8, 5), volume(5));

            var result = courier.completeOrder(orderId);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.courierMustHaveRightDistanceToOrder());
            assertThat(courier.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("отказывает, если заказ не назначен курьеру")
        void rejectsWhenOrderIsNotAssigned() {
            var courier = courier(location(5, 5));

            var result = courier.completeOrder(UUID.randomUUID());

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(Courier.Errors.orderDoesNotExist());
        }

        @Test
        @DisplayName("отказывает при повторном завершении того же заказа")
        void rejectsWhenAssignmentAlreadyCompleted() {
            var courier = courier(location(5, 5));
            var orderId = UUID.randomUUID();
            courier.takeOrder(orderId, location(5, 5), volume(5));
            courier.completeOrder(orderId);

            var result = courier.completeOrder(orderId);

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
