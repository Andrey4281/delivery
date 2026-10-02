package microarch.delivery.core.application.queries.dto;

import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {
    private final CourierRepository courierRepository;

    public GetAllCouriersQueryHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public List<CourierDto> handle() {
        return courierRepository.getAll().stream()
                .map(courier -> new CourierDto(
                        courier.getId(),
                        courier.getName(),
                        courier.getLocation().getX(),
                        courier.getLocation().getY()))
                .toList();
    }
}
