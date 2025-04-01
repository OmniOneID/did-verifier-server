package org.omnione.did.base.db.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.omnione.did.base.datamodel.enums.SymmetricPaddingType;

@Converter(autoApply = true)
public class SymmetricPaddingTypeConverter implements AttributeConverter<SymmetricPaddingType, String> {

    @Override
    public String convertToDatabaseColumn(SymmetricPaddingType attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public SymmetricPaddingType convertToEntityAttribute(String dbData) {
        return dbData != null ? SymmetricPaddingType.fromDisplayName(dbData) : null;
    }
}
