package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import microarch.delivery.core.domain.model.courier.AssignmentStatus;

@Converter
public class AssignmentStatusConverter implements AttributeConverter<AssignmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssignmentStatus attribute) {
        return attribute.name();
    }

    @Override
    public AssignmentStatus convertToEntityAttribute(String dbData) {
        return AssignmentStatus.valueOf(dbData);
    }
}
