package dev.ebullient.pockets.db;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PocketTypeConverter implements AttributeConverter<PocketType, String> {

    @Override
    public String convertToDatabaseColumn(PocketType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.slug;
    }

    @Override
    public PocketType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (PocketType t : PocketType.values()) {
            if (t.slug.equals(dbData)) {
                return t;
            }
        }
        return null;
    }
}
