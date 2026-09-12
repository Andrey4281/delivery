package microarch.delivery.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Location")
class LocationTest {

    private static Location location(int x, int y) {
        return Location.create(x, y).getValue();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт локацию для валидных координат")
        void createsLocationForValidCoordinates() {
            var result = Location.create(5, 7);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue().getX()).isEqualTo(5);
            assertThat(result.getValue().getY()).isEqualTo(7);
        }

        @ParameterizedTest(name = "x={0}, y={1} — валидно")
        @CsvSource({ "1, 1", "1, 10", "10, 1", "10, 10", "5, 5" })
        @DisplayName("принимает координаты на границах диапазона включительно")
        void acceptsBoundaryValues(int x, int y) {
            assertThat(Location.create(x, y).isSuccess()).isTrue();
        }

        @ParameterizedTest(name = "x={0}, y={1} -> {2}, поле: {3}")
        @CsvSource({ "0,  5,  value.must.be.greater.than, x", "-3, 5,  value.must.be.greater.than, x", "11, 5,  value.must.be.less.than,    x",
                "5,  0,  value.must.be.greater.than, y", "5,  -3, value.must.be.greater.than, y", "5,  11, value.must.be.less.than,    y" })
        @DisplayName("отклоняет координаты вне диапазона [1..10]")
        void rejectsOutOfRangeCoordinates(int x, int y, String expectedCode, String expectedField) {
            var result = Location.create(x, y);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo(expectedCode);
            assertThat(result.getError().getMessage()).contains(expectedField);
        }

        @Test
        @DisplayName("при нескольких невалидных координатах возвращает первую ошибку — по x")
        void returnsFirstErrorForMultipleInvalidCoordinates() {
            var result = Location.create(0, 11);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getMessage()).contains("x");
        }
    }

    @Nested
    @DisplayName("distance")
    class Distance {

        @Test
        @DisplayName("до самой себя равна нулю")
        void zeroToItself() {
            var location = location(4, 6);

            assertThat(location.distance(location)).isZero();
        }

        @Test
        @DisplayName("считает манхэттенское расстояние: |dx| + |dy|")
        void calculatesManhattanDistance() {
            assertThat(location(1, 1).distance(location(5, 4))).isEqualTo(7);
            assertThat(location(5, 4).distance(location(1, 1))).isEqualTo(7);
            assertThat(location(2, 3).distance(location(2, 9))).isEqualTo(6);
            assertThat(location(2, 3).distance(location(8, 3))).isEqualTo(6);
        }

        @Test
        @DisplayName("максимальное расстояние на поле 10x10 равно 18")
        void maxDistanceOnField() {
            assertThat(location(1, 1).distance(location(10, 10))).isEqualTo(18);
        }
    }

    @Nested
    @DisplayName("value object семантика")
    class ValueObjectSemantics {

        @Test
        @DisplayName("локации с одинаковыми координатами равны и имеют одинаковый hashCode")
        void equalLocationsHaveSameHashCode() {
            var first = location(3, 8);
            var second = location(3, 8);

            assertThat(first).isEqualTo(second);
            assertThat(first).hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("локации с разными координатами не равны")
        void differentLocationsAreNotEqual() {
            assertThat(location(3, 8)).isNotEqualTo(location(8, 3));
            assertThat(location(1, 5)).isNotEqualTo(location(2, 5));
            assertThat(location(5, 1)).isNotEqualTo(location(5, 2));
        }

        @Test
        @DisplayName("не равен null и объекту другого типа")
        void notEqualToNullAndOtherType() {
            assertThat(location(3, 8)).isNotEqualTo(null);
            assertThat(location(3, 8)).isNotEqualTo("3, 8");
        }

        @Test
        @DisplayName("сравнение: сначала по x, затем по y")
        void comparesByXThenByY() {
            assertThat(location(1, 9)).isLessThan(location(2, 1));
            assertThat(location(3, 4)).isLessThan(location(3, 5));
            assertThat(location(3, 5)).isEqualByComparingTo(location(3, 5));
            assertThat(location(9, 9)).isGreaterThan(location(9, 2));
        }
    }
}
