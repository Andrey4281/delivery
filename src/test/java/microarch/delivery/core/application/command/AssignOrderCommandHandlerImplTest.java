package microarch.delivery.core.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.OrderService;
import microarch.delivery.core.domain.services.OrderServiceImpl;
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

@DisplayName("AssignOrderCommandHandler")
@ExtendWith(MockitoExtension.class)
class AssignOrderCommandHandlerImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<Courier> courierCaptor;

    private AssignOrderCommandHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderService);
    }

    private Order createdOrder() {
        var location = Location.create(5, 5).getValue();
        return Order.create(UUID.randomUUID(), location, Volume.create(3).getValue()).getValue();
    }

    private Courier courierAt(int x, int y) {
        return Courier.create("Ivan", Location.create(x, y).getValue()).getValue();
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("назначает заказ на курьера и сохраняет изменения")
        void assignsOrderToCourierAndSavesChanges() {
            var order = createdOrder();
            var courier = courierAt(5, 6);
            when(orderRepository.getFirstInCreatedStatus()).thenReturn(Optional.of(order));
            when(courierRepository.getAll()).thenReturn(List.of(courier));
            when(orderService.assignOrder(order, List.of(courier))).thenReturn(Result.success(courier));
            var command = AssignOrderCommand.create().getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderRepository).update(orderCaptor.capture());
            assertThat(orderCaptor.getValue()).isSameAs(order);
            verify(courierRepository).update(courierCaptor.capture());
            assertThat(courierCaptor.getValue()).isSameAs(courier);
        }

        @Test
        @DisplayName("возвращает success, когда нет заказов в статусе CREATED")
        void returnsSuccessWhenNoCreatedOrders() {
            when(orderRepository.getFirstInCreatedStatus()).thenReturn(Optional.empty());
            var command = AssignOrderCommand.create().getValue();

            var result = handler.handle(command);

            assertThat(result.isSuccess()).isTrue();
            verify(orderService, never()).assignOrder(any(), anyList());
            verify(orderRepository, never()).update(orderCaptor.capture());
            verify(courierRepository, never()).update(courierCaptor.capture());
        }

        @Test
        @DisplayName("возвращает ошибку, когда диспетчеризация не удалась")
        void returnsErrorWhenDispatchFails() {
            var order = createdOrder();
            var courier = courierAt(5, 5);
            when(orderRepository.getFirstInCreatedStatus()).thenReturn(Optional.of(order));
            when(courierRepository.getAll()).thenReturn(List.of(courier));
            when(orderService.assignOrder(order, List.of(courier)))
                .thenReturn(Result.failure(OrderServiceImpl.Errors.allCouriersAreFullyBookedOrUnavailable()));
            var command = AssignOrderCommand.create().getValue();

            var result = handler.handle(command);

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().getCode()).isEqualTo("all.couriers.are.fully.booked.or.unavailable");
            verify(orderRepository, never()).update(order);
            verify(courierRepository, never()).update(courier);
        }
    }

    @Nested
    @DisplayName("создание команды")
    class CommandCreation {

        @Test
        @DisplayName("создаёт команду без полей")
        void createsCommandWithoutFields() {
            var result = AssignOrderCommand.create();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getValue()).isNotNull();
        }
    }
}
