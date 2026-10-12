package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Address;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
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

    @Mock
    private GeoClient geoClient;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private CreateOrderCommandHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new CreateOrderCommandHandlerImpl(orderRepository, geoClient);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("создаёт заказ и сохраняет в репозиторий")
        void createsOrderAndSavesToRepository() {
            var orderID = UUID.randomUUID();
            var command = CreateOrderCommand.create(orderID, "Russia", "Moscow", "Lenina", "10", "5", 3).getValue();
            var location = Location.create(5, 7).getValue();
            when(geoClient.getLocation(any(Address.class))).thenReturn(Result.success(location));

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(geoClient).getLocation(command.getAddress());
            verify(orderRepository).add(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getId()).isEqualTo(orderID);
            assertThat(savedOrder.getVolume().getValue()).isEqualTo(3);
            assertThat(savedOrder.getLocation()).isEqualTo(location);
            assertThat(savedOrder.getOrderStatus()).isEqualTo(microarch.delivery.core.domain.model.order.OrderStatus.CREATED);
        }

        @Test
        @DisplayName("сохраняет заказ с Location, полученной от GeoClient")
        void savesOrderWithLocationFromGeoClient() {
            var command = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "10", "5", 3).getValue();
            var location = Location.create(3, 4).getValue();
            when(geoClient.getLocation(any(Address.class))).thenReturn(Result.success(location));

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderRepository).add(orderCaptor.capture());
            var savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getLocation()).isEqualTo(location);
        }

        @Test
        @DisplayName("возвращает ошибку, когда GeoClient не смог определить Location")
        void returnsErrorWhenGeoClientFails() {
            var command = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "10", "5", 3).getValue();
            var error = GeneralErrors.valueIsRequired("location");
            when(geoClient.getLocation(any(Address.class))).thenReturn(Result.failure(error));

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError()).isEqualTo(error);
            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("возвращает ошибку при неудачном создании заказа")
        void returnsErrorWhenOrderCreationFails() {
            var command = CreateOrderCommand.create(null, "Russia", "Moscow", "Lenina", "10", "5", 3);

            assertThat(command.isFailure()).isTrue();
        }

        @Test
        @DisplayName("корректно обрабатывает валидный Address")
        void handlesValidAddress() {
            var orderID = UUID.randomUUID();
            var command = CreateOrderCommand.create(orderID, "Russia", "Moscow", "Lenina", "10", "5", 3).getValue();
            when(geoClient.getLocation(any(Address.class))).thenReturn(Result.success(Location.create(1, 1).getValue()));

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(geoClient).getLocation(command.getAddress());
            verify(orderRepository).add(orderCaptor.capture());
        }

        @Test
        @DisplayName("создаёт несколько заказов с Location от GeoClient")
        void createsMultipleOrdersWithLocationsFromGeoClient() {
            var command1 = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "10", "5", 3).getValue();

            var command2 = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Tverskaya", "8", "2", 4).getValue();

            var location1 = Location.create(2, 3).getValue();
            var location2 = Location.create(8, 9).getValue();
            when(geoClient.getLocation(command1.getAddress())).thenReturn(Result.success(location1));
            when(geoClient.getLocation(command2.getAddress())).thenReturn(Result.success(location2));

            var result1 = handler.handle(command1);
            var result2 = handler.handle(command2);

            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();

            verify(orderRepository, times(2)).add(orderCaptor.capture());
            var allOrders = orderCaptor.getAllValues();
            assertThat(allOrders).hasSize(2);
            assertThat(allOrders.get(0).getLocation()).isEqualTo(location1);
            assertThat(allOrders.get(1).getLocation()).isEqualTo(location2);
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду с валидными данными")
        void createsCommandWithValidData() {
            var orderID = UUID.randomUUID();
            var result = CreateOrderCommand.create(orderID, "Russia", "Moscow", "Lenina", "10", "5", 3);

            assertThat(result.isSuccess()).isTrue();
            var command = result.getValue();
            assertThat(command.getOrderID()).isEqualTo(orderID);
            assertThat(command.getVolume().getValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("отклоняет пустой orderID")
        void rejectsNullOrderID() {
            var result = CreateOrderCommand.create(null, "Russia", "Moscow", "Lenina", "10", "5", 3);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет пустой country")
        void rejectsEmptyCountry() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "", "Moscow", "Lenina", "10", "5", 3);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("отклоняет пустой city")
        void rejectsEmptyCity() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "", "Lenina", "10", "5", 3);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой street")
        void rejectsEmptyStreet() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "", "10", "5", 3);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой house")
        void rejectsEmptyHouse() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "", "5", 3);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет пустой apartment")
        void rejectsEmptyApartment() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "10", "", 3);

            assertThat(result.isFailure()).isTrue();
        }

        @Test
        @DisplayName("отклоняет невалидный volume")
        void rejectsInvalidVolume() {
            var result = CreateOrderCommand.create(UUID.randomUUID(), "Russia", "Moscow", "Lenina", "10", "5", 0);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.must.be.greater.than");
        }
    }
}
