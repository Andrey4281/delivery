package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

@DisplayName("CreateCourierCommandHandler")
@ExtendWith(MockitoExtension.class)
class CreateCourierCommandHandlerImplTest {

    @Mock
    private CourierRepository courierRepository;

    @Captor
    private ArgumentCaptor<Courier> courierCaptor;

    private CreateCourierCommandHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new CreateCourierCommandHandlerImpl(courierRepository);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("создаёт курьера и сохраняет в репозиторий")
        void createsCourierAndSavesToRepository() {
            var command = CreateCourierCommand.create("Иван").getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).add(courierCaptor.capture());
            var savedCourier = courierCaptor.getValue();
            assertThat(savedCourier.getName()).isEqualTo("Иван");
        }

        @Test
        @DisplayName("создаёт курьера в Location (1,1)")
        void createsCourierAtLocationOneOne() {
            var command = CreateCourierCommand.create("Иван").getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).add(courierCaptor.capture());
            var location = courierCaptor.getValue().getLocation();
            assertThat(location.getX()).isEqualTo(1);
            assertThat(location.getY()).isEqualTo(1);
        }

        @Test
        @DisplayName("создаёт курьера со стандартным maxVolume 20")
        void createsCourierWithDefaultMaxVolume() {
            var command = CreateCourierCommand.create("Иван").getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).add(courierCaptor.capture());
            assertThat(courierCaptor.getValue().getMaxVolume().getValue()).isEqualTo(20);
        }

        @Test
        @DisplayName("создаёт нескольких курьеров")
        void createsMultipleCouriers() {
            var command1 = CreateCourierCommand.create("Иван").getValue();
            var command2 = CreateCourierCommand.create("Пётр").getValue();

            var result1 = handler.handle(command1);
            var result2 = handler.handle(command2);

            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
            verify(courierRepository, times(2)).add(courierCaptor.capture());
            var allCouriers = courierCaptor.getAllValues();
            assertThat(allCouriers).hasSize(2);
            assertThat(allCouriers.get(0).getName()).isEqualTo("Иван");
            assertThat(allCouriers.get(1).getName()).isEqualTo("Пётр");
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду с валидным именем")
        void createsCommandWithValidName() {
            var result = CreateCourierCommand.create("Иван");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue().getName()).isEqualTo("Иван");
        }

        @Test
        @DisplayName("отклоняет null name")
        void rejectsNullName() {
            var result = CreateCourierCommand.create(null);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет пустую строку в name")
        void rejectsEmptyName() {
            var result = CreateCourierCommand.create("");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет name из одних пробелов")
        void rejectsBlankName() {
            var result = CreateCourierCommand.create("   ");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }
    }
}
