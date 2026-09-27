package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import microarch.delivery.core.domain.model.order.OrderStatus;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        return attribute.name();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        return OrderStatus.valueOf(dbData);
    }
}
