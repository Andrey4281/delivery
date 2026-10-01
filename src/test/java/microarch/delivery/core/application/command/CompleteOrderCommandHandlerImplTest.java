package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
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

@DisplayName("CompleteOrderCommandHandler")
@ExtendWith(MockitoExtension.class)
class CompleteOrderCommandHandlerImplTest {

    private static final Location ORDER_LOCATION = Location.create(5, 5).getValue();

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<Courier> courierCaptor;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private CompleteOrderCommandHandlerImpl handler;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository);
        orderId = UUID.randomUUID();
    }

    private Order createdOrder() {
        return Order.create(UUID.randomUUID(), ORDER_LOCATION, Volume.create(3).getValue()).getValue();
    }

    private Order assignedOrder() {
        var order = createdOrder();
        order.assign();
        return order;
    }

    private Courier courierWithAssignment(int x, int y) {
        var courier = Courier.create("Ivan", Location.create(x, y).getValue()).getValue();
        courier.takeOrder(orderId, ORDER_LOCATION, Volume.create(3).getValue());
        return courier;
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("завершает assignment курьера и заказ, сохраняет изменения")
        void completesAssignmentAndOrderAndSavesChanges() {
            var courier = courierWithAssignment(5, 6);
            var order = assignedOrder();
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            when(orderRepository.getById(orderId)).thenReturn(Optional.of(order));
            var command = CompleteOrderCommand.create(courier.getId(), orderId).getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(courierRepository).update(courierCaptor.capture());
            assertThat(courierCaptor.getValue()).isSameAs(courier);
            verify(orderRepository).update(orderCaptor.capture());
            assertThat(orderCaptor.getValue()).isSameAs(order);
        }

        @Test
        @DisplayName("возвращает ошибку, когда курьер не найден")
        void returnsErrorWhenCourierNotFound() {
            var courierId = UUID.randomUUID();
            when(courierRepository.getById(courierId)).thenReturn(Optional.empty());
            var command = CompleteOrderCommand.create(courierId, orderId).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("record.not.found");
            verify(orderRepository, never()).getById(any());
            verify(courierRepository, never()).update(any());
            verify(orderRepository, never()).update(any());
        }

        @Test
        @DisplayName("возвращает ошибку, когда заказ не найден")
        void returnsErrorWhenOrderNotFound() {
            var courier = courierWithAssignment(5, 6);
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            when(orderRepository.getById(orderId)).thenReturn(Optional.empty());
            var command = CompleteOrderCommand.create(courier.getId(), orderId).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("record.not.found");
            verify(courierRepository, never()).update(any());
            verify(orderRepository, never()).update(any());
        }

        @Test
        @DisplayName("возвращает ошибку, когда курьер далеко от точки доставки")
        void returnsErrorWhenCourierIsFarFromOrder() {
            var courier = courierWithAssignment(5, 9);
            var order = assignedOrder();
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            when(orderRepository.getById(orderId)).thenReturn(Optional.of(order));
            var command = CompleteOrderCommand.create(courier.getId(), orderId).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("courier.must.have.right.distance.to.order");
            verify(courierRepository, never()).update(any());
            verify(orderRepository, never()).update(any());
        }

        @Test
        @DisplayName("возвращает ошибку, когда заказ не в статусе ASSIGNED")
        void returnsErrorWhenOrderIsNotAssigned() {
            var courier = courierWithAssignment(5, 6);
            var order = createdOrder();
            when(courierRepository.getById(courier.getId())).thenReturn(Optional.of(courier));
            when(orderRepository.getById(orderId)).thenReturn(Optional.of(order));
            var command = CompleteOrderCommand.create(courier.getId(), orderId).getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("order.must.be.assigned.to.complete");
            verify(courierRepository, never()).update(any());
            verify(orderRepository, never()).update(any());
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду с валидными данными")
        void createsCommandWithValidData() {
            var courierId = UUID.randomUUID();
            var orderId = UUID.randomUUID();

            var result = CompleteOrderCommand.create(courierId, orderId);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue().getCourierId()).isEqualTo(courierId);
            assertThat(result.getValue().getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("возвращает ошибку при null courierId")
        void returnsErrorWhenCourierIdIsNull() {
            var result = CompleteOrderCommand.create(null, UUID.randomUUID());

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }

        @Test
        @DisplayName("возвращает ошибку при null orderId")
        void returnsErrorWhenOrderIdIsNull() {
            var result = CompleteOrderCommand.create(UUID.randomUUID(), null);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("value.is.required");
        }
    }
}
