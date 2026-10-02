package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("CreateOrderCommandHandler")
@ExtendWith(MockitoExtension.class)
class CreateOrderCommandHandlerImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private CreateOrderCommandHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderCommandHandlerImpl(orderRepository);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("создаёт заказ и сохраняет в репозиторий")
        void createsOrderAndSavesToRepository() {
            var orderID = UUID.randomUUID();
            var command = CreateOrderCommand.create(
                orderID,
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            ).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderRepository).add(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getId()).isEqualTo(orderID);
            assertThat(savedOrder.getVolume().getValue()).isEqualTo(3);
            assertThat(savedOrder.getOrderStatus()).isEqualTo(microarch.delivery.core.domain.model.order.OrderStatus.CREATED);
        }

        @Test
        @DisplayName("создаёт заказ со случайной Location в допустимом диапазоне")
        void createsOrderWithRandomLocationInRange() {
            var command = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            ).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderRepository).add(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            var location = savedOrder.getLocation();
            assertThat(location.getX()).isBetween(1, 10);
            assertThat(location.getY()).isBetween(1, 10);
        }

        @Test
        @DisplayName("возвращает ошибку при неудачном создании заказа")
        void returnsErrorWhenOrderCreationFails() {
            var command = CreateOrderCommand.create(
                null,
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            );

            assertThat(command.isFailure()).isTrue();
        }

        @Test
        @DisplayName("корректно обрабатывает валидный Address")
        void handlesValidAddress() {
            var orderID = UUID.randomUUID();
            var command = CreateOrderCommand.create(
                orderID,
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            ).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderRepository).add(orderCaptor.capture());
        }

        @Test
        @DisplayName("создаёт несколько заказов с разными случайными Location")
        void createsMultipleOrdersWithDifferentRandomLocations() {
            var command1 = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            ).getValue();

            var command2 = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            ).getValue();

            var result1 = handler.handle(command1);
            var result2 = handler.handle(command2);

            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();

            verify(orderRepository, times(2)).add(orderCaptor.capture());
            var allOrders = orderCaptor.getAllValues();
            assertThat(allOrders).hasSize(2);
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду с валидными данными")
        void createsCommandWithValidData() {
            var orderID = UUID.randomUUID();
            var result = CreateOrderCommand.create(
                orderID,
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            );

            assertThat(result.isSuccess()).isTrue();
            var command = result.getValue();
            assertThat(command.getOrderID()).isEqualTo(orderID);
            assertThat(command.getVolume().getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("отклоняет пустой orderID")
        void rejectsNullOrderID() {
            var result = CreateOrderCommand.create(
                null,
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет пустой country")
        void rejectsEmptyCountry() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "",
                "Moscow",
                "Lenina",
                "10",
                "5",
                3
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет пустой city")
        void rejectsEmptyCity() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "",
                "Lenina",
                "10",
                "5",
                3
            );

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой street")
        void rejectsEmptyStreet() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "",
                "10",
                "5",
                3
            );

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой house")
        void rejectsEmptyHouse() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "",
                "5",
                3
            );

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой apartment")
        void rejectsEmptyApartment() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "",
                3
            );

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет невалидный volume")
        void rejectsInvalidVolume() {
            var result = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Russia",
                "Moscow",
                "Lenina",
                "10",
                "5",
                0
            );

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.than");
        }
    }
}
