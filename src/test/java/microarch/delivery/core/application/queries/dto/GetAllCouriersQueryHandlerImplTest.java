package microarch.delivery.core.application.queries.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetAllCouriersQueryHandler")
@ExtendWith(MockitoExtension.class)
class GetAllCouriersQueryHandlerImplTest {

    @Mock
    private CourierRepository courierRepository;

    private GetAllCouriersQueryHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new GetAllCouriersQueryHandlerImpl(courierRepository);
    }

    private Courier courierAt(String name, int x, int y) {
        return Courier.create(name, Location.create(x, y).getValue()).getValue();
    }

    @Test
    @DisplayName("возвращает DTO всех курьеров из репозитория")
    void returnsDtoForAllCouriers() {
        var first = courierAt("Ivan", 1, 2);
        var second = courierAt("Petr", 9, 8);
        when(courierRepository.getAll()).thenReturn(List.of(first, second));

        var result = handler.handle();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).courierId()).isEqualTo(first.getId());
        assertThat(result.get(0).courierName()).isEqualTo("Ivan");
        assertThat(result.get(0).x()).isEqualTo(1);
        assertThat(result.get(0).y()).isEqualTo(2);
        assertThat(result.get(1).courierId()).isEqualTo(second.getId());
        assertThat(result.get(1).courierName()).isEqualTo("Petr");
        assertThat(result.get(1).x()).isEqualTo(9);
        assertThat(result.get(1).y()).isEqualTo(8);
    }

    @Test
    @DisplayName("возвращает пустой список, когда курьеров нет")
    void returnsEmptyListWhenNoCouriers() {
        when(courierRepository.getAll()).thenReturn(List.of());

        var result = handler.handle();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("не возвращает курьеров, которых нет в репозитории")
    void returnsOnlyCouriersFromRepository() {
        var courier = courierAt("Ivan", 5, 5);
        when(courierRepository.getAll()).thenReturn(List.of(courier));

        var result = handler.handle();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).courierId()).isNotEqualTo(UUID.randomUUID());
        assertThat(result.get(0).courierId()).isEqualTo(courier.getId());
    }
}
