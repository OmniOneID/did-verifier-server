package org.omnione.did.base.db.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.omnione.did.base.datamodel.enums.SymmetricCipherType;

@Converter(autoApply = true)
public class SymmetricCipherTypeConverter implements AttributeConverter<SymmetricCipherType, String> {

    @Override
    public String convertToDatabaseColumn(SymmetricCipherType attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public SymmetricCipherType convertToEntityAttribute(String dbData) {
        return dbData != null ? SymmetricCipherType.fromDisplayName(dbData) : null;
    }
}

