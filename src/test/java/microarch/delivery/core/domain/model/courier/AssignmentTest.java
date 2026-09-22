package microarch.delivery.core.domain.model.courier;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Assignment")
class AssignmentTest {

    private static Location location(int x, int y) {
        return Location.create(x, y).getValue();
    }

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    private static Assignment assignment(Location location) {
        return Assignment.create(UUID.randomUUID(), location, volume(1)).getValue();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт назначение со статусом ASSIGNED и сохраняет все аргументы")
        void createsAssignmentWithAssignedStatus() {
            var orderId = UUID.randomUUID();
            var location = location(5, 5);
            var volume = volume(3);

            var result = Assignment.create(orderId, location, volume);

            assertThat(result.isSuccess()).isTrue();
            var assignment = result.getValue();
            assertThat(assignment.getOrderId()).isEqualTo(orderId);
            assertThat(assignment.getLocation()).isEqualTo(location);
            assertThat(assignment.getVolume()).isEqualTo(volume);
            assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("бросает NPE при null-аргументах (fail-fast)")
        void throwsOnNullArguments() {
            var orderId = UUID.randomUUID();
            var location = location(5, 5);
            var volume = volume(1);

            assertThatThrownBy(() -> Assignment.create(null, location, volume)).isInstanceOf(NullPointerException.class).hasMessageContaining("orderId");
            assertThatThrownBy(() -> Assignment.create(orderId, null, volume)).isInstanceOf(NullPointerException.class).hasMessageContaining("location");
            assertThatThrownBy(() -> Assignment.create(orderId, location, null)).isInstanceOf(NullPointerException.class).hasMessageContaining("volume");
        }
    }
}
