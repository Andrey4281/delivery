package microarch.delivery.core.application.queries.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import microarch.delivery.adapters.out.postgres.OrderJpaRepository;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GetNotCompletedOrdersQueryHandler")
@ExtendWith(MockitoExtension.class)
class GetNotCompletedOrdersQueryHandlerImplTest {

    @Mock
    private OrderJpaRepository orderRepository;

    private GetNotCompletedOrdersQueryHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new GetNotCompletedOrdersQueryHandlerImpl(orderRepository);
    }

    private Order createdOrder(int x, int y) {
        return Order.create(UUID.randomUUID(), Location.create(x, y).getValue(), Volume.create(3).getValue()).getValue();
    }

    private Order assignedOrder(int x, int y) {
        var order = createdOrder(x, y);
        order.assign();
        return order;
    }

    @Test
    @DisplayName("возвращает DTO заказов в статусах Created и Assigned")
    void returnsDtoForCreatedAndAssignedOrders() {
        var created = createdOrder(1, 2);
        var assigned = assignedOrder(9, 8);
        when(orderRepository.findAllByOrderStatusOrOrderStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED)).thenReturn(List.of(created, assigned));

        var result = handler.handle();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderId()).isEqualTo(created.getId());
        assertThat(result.get(0).x()).isEqualTo(1);
        assertThat(result.get(0).y()).isEqualTo(2);
        assertThat(result.get(1).orderId()).isEqualTo(assigned.getId());
        assertThat(result.get(1).x()).isEqualTo(9);
        assertThat(result.get(1).y()).isEqualTo(8);
    }

    @Test
    @DisplayName("запрашивает у репозитория заказы в статусах Created или Assigned")
    void queriesRepositoryForCreatedOrAssignedStatus() {
        when(orderRepository.findAllByOrderStatusOrOrderStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED))
                .thenReturn(List.of());

        handler.handle();

        verify(orderRepository).findAllByOrderStatusOrOrderStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED);
    }

    @Test
    @DisplayName("возвращает пустой список, когда незавершённых заказов нет")
    void returnsEmptyListWhenNoNotCompletedOrders() {
        when(orderRepository.findAllByOrderStatusOrOrderStatus(OrderStatus.CREATED, OrderStatus.ASSIGNED))
                .thenReturn(List.of());

        var result = handler.handle();

        assertThat(result).isEmpty();
    }
}
