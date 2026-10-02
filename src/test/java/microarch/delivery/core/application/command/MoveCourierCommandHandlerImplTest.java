package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("MoveCourierCommandHandler")
@ExtendWith(MockitoExtension.class)
class MoveCourierCommandHandlerImplTest {

    @Mock
    private CourierRepository courierRepository;

    @Captor
    private ArgumentCaptor<Courier> courierCaptor;

    private MoveCourierCommandHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new MoveCourierCommandHandlerImpl(courierRepository);
    }

    private Courier courierAt(int x, int y) {
        return Courier.create("Ivan", Location.create(x, y).getValue()).getValue();
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("перемещает курьера в соседнюю клетку и сохраняет в репозиторий")
        void movesCourierToAdjacentCellAndSaves() {
            var courier = courierAt(5, 5);
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            var command = MoveCourierCommand.create(courier.getId(), 5, 6).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).update(courierCaptor.capture());
            var savedCourier = courierCaptor.getValue();
            assertThat(savedCourier.getLocation().getX()).isEqualTo(5);
            assertThat(savedCourier.getLocation().getY()).isEqualTo(6);
        }

        @Test
        @DisplayName("не перемещает курьера в несоседнюю клетку")
        void doesNotMoveCourierToFarCell() {
            var courier = courierAt(5, 5);
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            var command = MoveCourierCommand.create(courier.getId(), 5, 8).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("the.courier.can.move.only.to.an.adjacent.cell");
            verify(courierRepository, never()).update(courier);
        }

        @Test
        @DisplayName("возвращает ошибку, когда курьер не найден")
        void returnsErrorWhenCourierNotFound() {
            var courierId = UUID.randomUUID();
            when(courierRepository.getById(courierId)).thenReturn(Optional.empty());
            var command = MoveCourierCommand.create(courierId, 5, 6).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("record.not.found");
            verify(courierRepository, never()).update(courierCaptor.capture());
        }

        @Test
        @DisplayName("перемещение в ту же клетку успешно")
        void movesToSameCellSuccessfully() {
            var courier = courierAt(5, 5);
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            var command = MoveCourierCommand.create(courier.getId(), 5, 5).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).update(courier);
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду с валидными данными")
        void createsCommandWithValidData() {
            var courierId = UUID.randomUUID();
            var result = MoveCourierCommand.create(courierId, 5, 6);

            assertThat(result.isSuccess()).isTrue();
            var command = result.getValue();
            assertThat(command.getCourierId()).isEqualTo(courierId);
            assertThat(command.getLocation().getX()).isEqualTo(5);
            assertThat(command.getLocation().getY()).isEqualTo(6);
        }

        @Test
        @DisplayName("отклоняет пустой courierId")
        void rejectsNullCourierId() {
            var result = MoveCourierCommand.create(null, 5, 6);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет координаты меньше допустимых")
        void rejectsCoordinatesBelowRange() {
            var result = MoveCourierCommand.create(UUID.randomUUID(), 0, 6);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.than");
        }

        @Test
        @DisplayName("отклоняет координаты больше допустимых")
        void rejectsCoordinatesAboveRange() {
            var result = MoveCourierCommand.create(UUID.randomUUID(), 5, 11);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.must.be.less.than");
        }
    }
}
