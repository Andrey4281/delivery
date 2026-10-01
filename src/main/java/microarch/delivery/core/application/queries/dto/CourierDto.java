package microarch.delivery.core.application.queries.dto;

import java.util.UUID;

public record CourierDto(UUID courierId, String courierName, int x, int y) {
}
