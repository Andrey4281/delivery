package microarch.delivery.adapters.out.postgres;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.domain.model.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderRepositoryImpl.class)
@ImportTestcontainers(AbstractPostgresContainer.class)
@DisplayName("OrderRepositoryImpl (PostgreSQL, Testcontainers)")
class OrderRepositoryImplTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Location location;
    private Volume volume;

    @BeforeEach
    void setUp() {
        location = Location.create(5, 5).getValue();
        volume = Volume.create(3).getValue();
    }

    private Order newOrder() {
        return Order.create(UUID.randomUUID(), location, volume).getValue();
    }

    private Order saveAndClear(Order order) {
        entityManager.persist(order);
        entityManager.flush();
        entityManager.clear();
        return order;
    }

    @Nested
    @Transactional
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("сохраняет заказ и возвращает его с присвоенным id")
        void savesOrderAndReturnsItWithId() {
            var order = newOrder();

            var saved = orderRepository.add(order);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getLocation()).isEqualTo(location);
            assertThat(saved.getVolume()).isEqualTo(volume);
            assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("сохранённый заказ читается из базы со всеми полями")
        void savedOrderIsPersistedWithAllFields() {
            var order = newOrder();
            orderRepository.add(order);
            entityManager.flush();
            entityManager.clear();

            var reloaded = entityManager.find(Order.class, order.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getLocation()).isEqualTo(location);
            assertThat(reloaded.getVolume()).isEqualTo(volume);
            assertThat(reloaded.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }
    }

    @Nested
    @Transactional
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("возвращает сохранённый заказ по идентификатору")
        void returnsSavedOrder() {
            var order = saveAndClear(newOrder());

            var found = orderRepository.getById(order.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(order.getId());
            assertThat(found.get().getLocation()).isEqualTo(location);
            assertThat(found.get().getVolume()).isEqualTo(volume);
            assertThat(found.get().getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("возвращает empty для неизвестного идентификатора")
        void returnsEmptyForUnknownId() {
            var found = orderRepository.getById(UUID.randomUUID());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getFirstInCreatedStatus")
    class GetFirstInCreatedStatus {

        @Test
        @DisplayName("возвращает empty, когда новых заказов нет")
        void returnsEmptyWhenNoCreatedOrders() {
            var found = orderRepository.getFirstInCreatedStatus();

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("возвращает один из заказов в статусе Created")
        void returnsOneOfCreatedOrders() {
            var first = saveAndClear(newOrder());
            var second = saveAndClear(newOrder());

            var found = orderRepository.getFirstInCreatedStatus();

            assertThat(found).isPresent();
            assertThat(found.get().getOrderStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(found.get().getId()).isIn(first.getId(), second.getId());
        }

        @Test
        @DisplayName("не возвращает заказы в других статусах")
        void doesNotReturnOrdersInOtherStatuses() {
            var assigned = newOrder();
            assigned.assign();
            saveAndClear(assigned);

            var found = orderRepository.getFirstInCreatedStatus();

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getAllInAssignedStatus")
    class GetAllInAssignedStatus {

        @Test
        @DisplayName("возвращает empty, когда назначенных заказов нет")
        void returnsEmptyWhenNoAssignedOrders() {
            saveAndClear(newOrder());

            var found = orderRepository.getAllInAssignedStatus();

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("возвращает только заказы в статусе Assigned")
        void returnsOnlyAssignedOrders() {
            var assigned = newOrder();
            assigned.assign();
            saveAndClear(assigned);
            saveAndClear(newOrder());

            var found = orderRepository.getAllInAssignedStatus();

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getId()).isEqualTo(assigned.getId());
            assertThat(found.get(0).getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("возвращает все назначенные заказы")
        void returnsAllAssignedOrders() {
            var first = newOrder();
            first.assign();
            var second = newOrder();
            second.assign();
            saveAndClear(first);
            saveAndClear(second);

            var found = orderRepository.getAllInAssignedStatus();

            assertThat(found).extracting(Order::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
        }
    }

    @Nested
    @Transactional
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("сохраняет изменение статуса заказа")
        void persistsStatusChange() {
            var order = saveAndClear(newOrder());

            var reloaded = entityManager.find(Order.class, order.getId());
            reloaded.assign();
            orderRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var afterUpdate = entityManager.find(Order.class, order.getId());
            assertThat(afterUpdate.getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("обновлённый заказ читается через репозиторий")
        void updatedOrderIsVisibleViaRepository() {
            var order = saveAndClear(newOrder());

            var reloaded = entityManager.find(Order.class, order.getId());
            reloaded.assign();
            orderRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var found = orderRepository.getById(order.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getOrderStatus()).isEqualTo(OrderStatus.ASSIGNED);
        }

        @Test
        @DisplayName("после обновления до Assigned заказ попадает в выборку назначенных")
        void updatedOrderAppearsInAssignedQuery() {
            var order = saveAndClear(newOrder());

            var reloaded = entityManager.find(Order.class, order.getId());
            reloaded.assign();
            orderRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            List<Order> assigned = orderRepository.getAllInAssignedStatus();
            assertThat(assigned).extracting(Order::getId).containsExactly(order.getId());
        }
    }
}
