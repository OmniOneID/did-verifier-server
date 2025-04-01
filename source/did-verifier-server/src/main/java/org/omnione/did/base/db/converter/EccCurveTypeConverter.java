package org.omnione.did.base.db.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.omnione.did.base.datamodel.enums.EccCurveType;


@Converter(autoApply = true)
public class EccCurveTypeConverter implements AttributeConverter<EccCurveType, String> {

    @Override
    public String convertToDatabaseColumn(EccCurveType attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public EccCurveType convertToEntityAttribute(String dbData) {
        return dbData != null ? EccCurveType.fromValue(dbData) : null;
    }
}