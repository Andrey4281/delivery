package microarch.delivery.adapters.out.postgres;

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
import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CourierRepositoryImpl.class)
@ImportTestcontainers(AbstractPostgresContainer.class)
@DisplayName("CourierRepositoryImpl (PostgreSQL, Testcontainers)")
class CourierRepositoryImplTest {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Location location;

    @BeforeEach
    void setUp() {
        location = Location.create(5, 5).getValue();
    }

    private static Volume volume(int value) {
        return Volume.create(value).getValue();
    }

    private Courier newCourier() {
        return Courier.create("Иван", location).getValue();
    }

    private Courier saveAndClear(Courier courier) {
        entityManager.persist(courier);
        entityManager.flush();
        entityManager.clear();
        return courier;
    }

    @Nested
    @Transactional
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("сохраняет курьера без назначений и возвращает его с присвоенным id")
        void savesCourierWithoutAssignmentsAndReturnsItWithId() {
            var courier = newCourier();

            var saved = courierRepository.add(courier);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Иван");
            assertThat(saved.getLocation()).isEqualTo(location);
            assertThat(saved.getMaxVolume()).isEqualTo(volume(20));
            assertThat(saved.getAssignments()).isEmpty();
        }

        @Test
        @DisplayName("сохранённый курьер читается из базы со всеми полями и без назначений")
        void savedCourierIsPersistedWithAllFields() {
            var courier = courierRepository.add(newCourier());
            entityManager.flush();
            entityManager.clear();

            var reloaded = entityManager.find(Courier.class, courier.getId());

            assertThat(reloaded).isNotNull();
            assertThat(reloaded.getName()).isEqualTo("Иван");
            assertThat(reloaded.getLocation()).isEqualTo(location);
            assertThat(reloaded.getMaxVolume()).isEqualTo(volume(20));
            assertThat(reloaded.getAssignments()).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("возвращает сохранённого курьера по идентификатору")
        void returnsSavedCourier() {
            var courier = saveAndClear(newCourier());

            var found = courierRepository.getById(courier.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(courier.getId());
            assertThat(found.get().getName()).isEqualTo("Иван");
            assertThat(found.get().getLocation()).isEqualTo(location);
            assertThat(found.get().getAssignments()).isEmpty();
        }

        @Test
        @DisplayName("возвращает курьера вместе со списком его назначений")
        void returnsCourierWithAssignments() {
            var courier = newCourier();
            var firstOrderId = UUID.randomUUID();
            var secondOrderId = UUID.randomUUID();
            courier.takeOrder(firstOrderId, Location.create(6, 5).getValue(), volume(5));
            courier.takeOrder(secondOrderId, Location.create(5, 6).getValue(), volume(10));
            saveAndClear(courier);

            var found = courierRepository.getById(courier.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getAssignments()).hasSize(2);
            assertThat(found.get().getAssignments()).extracting(a -> a.getOrderId()).containsExactlyInAnyOrder(firstOrderId, secondOrderId);
            assertThat(found.get().getAssignments()).allSatisfy(a -> {
                assertThat(a.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
                assertThat(a.getLocation()).isIn(Location.create(6, 5).getValue(), Location.create(5, 6).getValue());
            });
        }

        @Test
        @DisplayName("возвращает empty для неизвестного идентификатора")
        void returnsEmptyForUnknownId() {
            var found = courierRepository.getById(UUID.randomUUID());

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("возвращает пустой список, когда курьеров нет")
        void returnsEmptyListWhenNoCouriers() {
            var found = courierRepository.getAll();

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("возвращает всех курьеров вместе с их назначениями")
        void returnsAllCouriersWithAssignments() {
            var first = newCourier();
            var firstOrderId = UUID.randomUUID();
            first.takeOrder(firstOrderId, Location.create(6, 5).getValue(), volume(5));
            saveAndClear(first);
            var second = Courier.create("Пётр", Location.create(9, 9).getValue()).getValue();
            saveAndClear(second);

            var found = courierRepository.getAll();

            assertThat(found).extracting(Courier::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
            var firstFound = found.stream().filter(c -> c.getId().equals(first.getId())).findFirst().orElseThrow();
            assertThat(firstFound.getAssignments()).hasSize(1);
            assertThat(firstFound.getAssignments().get(0).getOrderId()).isEqualTo(firstOrderId);
            var secondFound = found.stream().filter(c -> c.getId().equals(second.getId())).findFirst().orElseThrow();
            assertThat(secondFound.getAssignments()).isEmpty();
        }
    }

    @Nested
    @Transactional
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("сохраняет изменение локации курьера")
        void persistsLocationChange() {
            var courier = saveAndClear(newCourier());

            var reloaded = entityManager.find(Courier.class, courier.getId());
            reloaded.move(Location.create(6, 5).getValue());
            courierRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var afterUpdate = entityManager.find(Courier.class, courier.getId());
            assertThat(afterUpdate.getLocation()).isEqualTo(Location.create(6, 5).getValue());
        }

        @Test
        @DisplayName("сохраняет добавленные назначения курьера")
        void persistsAddedAssignments() {
            var courier = saveAndClear(newCourier());

            var reloaded = entityManager.find(Courier.class, courier.getId());
            var newOrderId = UUID.randomUUID();
            reloaded.takeOrder(newOrderId, Location.create(6, 5).getValue(), volume(5));
            courierRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var afterUpdate = courierRepository.getById(courier.getId()).orElseThrow();
            assertThat(afterUpdate.getAssignments()).hasSize(1);
            assertThat(afterUpdate.getAssignments().get(0).getOrderId()).isEqualTo(newOrderId);
            assertThat(afterUpdate.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        }

        @Test
        @DisplayName("сохраняет завершение назначения (статус Completed)")
        void persistsCompletedAssignmentStatus() {
            var courier = newCourier();
            var orderId = UUID.randomUUID();
            courier.takeOrder(orderId, Location.create(5, 5).getValue(), volume(5));
            saveAndClear(courier);

            var reloaded = entityManager.find(Courier.class, courier.getId());
            reloaded.completeOrder(orderId);
            courierRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var afterUpdate = courierRepository.getById(courier.getId()).orElseThrow();
            assertThat(afterUpdate.getAssignments()).hasSize(1);
            assertThat(afterUpdate.getAssignments().get(0).getOrderId()).isEqualTo(orderId);
            assertThat(afterUpdate.getAssignments().get(0).getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("обновлённый курьер читается через репозиторий")
        void updatedCourierIsVisibleViaRepository() {
            var courier = saveAndClear(newCourier());

            var reloaded = entityManager.find(Courier.class, courier.getId());
            reloaded.move(Location.create(6, 5).getValue());
            courierRepository.update(reloaded);
            entityManager.flush();
            entityManager.clear();

            var found = courierRepository.getById(courier.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getLocation()).isEqualTo(Location.create(6, 5).getValue());
        }
    }
}
