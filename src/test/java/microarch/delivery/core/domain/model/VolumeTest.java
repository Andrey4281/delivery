package microarch.delivery.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Volume")
class VolumeTest {

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("создаёт объём для положительного значения")
        void createsVolumeForPositiveValue() {
            var result = Volume.create(7);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue().getValue()).isEqualTo(7);
        }

        @Test
        @DisplayName("минимальная граница 1 включительно")
        void acceptsMinBoundary() {
            assertThat(Volume.create(1).isSuccess()).isTrue();
        }

        @Test
        @DisplayName("верхней границы нет — принимает большие значения")
        void acceptsLargeValues() {
            assertThat(Volume.create(1000).isSuccess()).isTrue();
        }

        @ParameterizedTest(name = "value={0} — невалидно")
        @ValueSource(ints = { 0, -1, -100 })
        @DisplayName("отклоняет значения меньше 1")
        void rejectsValuesLessThanOne(int value) {
            var result = Volume.create(value);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.than");
            assertThat(result.getError().getMessage()).contains("value").contains("1");
        }
    }

    @Nested
    @DisplayName("value object семантика")
    class ValueObjectSemantics {

        @Test
        @DisplayName("объёмы с одинаковым значением равны и имеют одинаковый hashCode")
        void equalVolumesHaveSameHashCode() {
            assertThat(volume(5)).isEqualTo(volume(5));
            assertThat(volume(5)).hasSameHashCodeAs(volume(5));
        }

        @Test
        @DisplayName("объёмы с разными значениями не равны")
        void differentVolumesAreNotEqual() {
            assertThat(volume(3)).isNotEqualTo(volume(4));
        }

        @Test
        @DisplayName("не равен null и объекту другого типа")
        void notEqualToNullAndOtherType() {
            assertThat(volume(3)).isNotEqualTo(null);
            assertThat(volume(3)).isNotEqualTo("3");
        }

        @Test
        @DisplayName("сравнение по значению")
        void comparesByValue() {
            assertThat(volume(2)).isLessThan(volume(9));
            assertThat(volume(9)).isGreaterThan(volume(2));
            assertThat(volume(4)).isEqualByComparingTo(volume(4));
        }
    }

    @Nested
    @DisplayName("сравнения isLessThan / isGreaterThan / isLessOrEqual / isGreaterOrEqual")
    class Comparisons {

        @Test
        @DisplayName("для неравных объёмов строгие и нестрогие сравнения согласованы")
        void comparisonsForUnequalVolumes() {
            var smaller = volume(2);
            var bigger = volume(8);

            assertThat(smaller.isLessThan(bigger)).isTrue();
            assertThat(smaller.isGreaterThan(bigger)).isFalse();
            assertThat(smaller.isLessOrEqual(bigger)).isTrue();
            assertThat(smaller.isGreaterOrEqual(bigger)).isFalse();

            assertThat(bigger.isLessThan(smaller)).isFalse();
            assertThat(bigger.isGreaterThan(smaller)).isTrue();
            assertThat(bigger.isLessOrEqual(smaller)).isFalse();
            assertThat(bigger.isGreaterOrEqual(smaller)).isTrue();
        }

        @Test
        @DisplayName("для равных объёмов нестрогие сравнения истинны, строгие ложны")
        void comparisonsForEqualVolumes() {
            var first = volume(6);
            var second = volume(6);

            assertThat(first.isLessThan(second)).isFalse();
            assertThat(first.isGreaterThan(second)).isFalse();
            assertThat(first.isLessOrEqual(second)).isTrue();
            assertThat(first.isGreaterOrEqual(second)).isTrue();
        }

        @Test
        @DisplayName("работают на минимальной границе")
        void comparisonsOnMinBoundary() {
            assertThat(volume(1).isLessThan(volume(2))).isTrue();
            assertThat(volume(2).isGreaterOrEqual(volume(1))).isTrue();
        }
    }
}
