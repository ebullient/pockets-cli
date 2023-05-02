package dev.ebullient.pockets.db;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;
import jakarta.persistence.AttributeConverter;

public class PocketsConverter {

    static <T> T convertToEntityAttribute(String dbData, TypeReference<T> type) {
        try {
            return Transform.JSON.readValue(dbData, type);
        } catch (JsonProcessingException e) {
            Tui.errorf(e, "Unable to parse configuration %s", dbData);
        }
        return null;
    }

    static <T> T convertToEntityAttribute(String dbData, Class<T> type) {
        try {
            return Transform.JSON.readValue(dbData, type);
        } catch (JsonProcessingException e) {
            Tui.errorf(e, "Unable to parse configuration %s", dbData);
        }
        return null;
    }

    static <T> String convertToDatabaseColumn(T attribute) {
        try {
            return Transform.JSON.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            Tui.errorf(e, "Unable to write configuration %s", attribute);
        }
        return null;
    }

    static class PocketDetailsConverter implements AttributeConverter<PocketDetails, String> {
        @Override
        public String convertToDatabaseColumn(PocketDetails attribute) {
            return PocketsConverter.convertToDatabaseColumn(attribute);
        }

        @Override
        public PocketDetails convertToEntityAttribute(String dbData) {
            return PocketsConverter.convertToEntityAttribute(dbData, PocketDetails.class);
        }
    }

    static class ItemDetailsConverter implements AttributeConverter<ItemDetails, String> {
        @Override
        public String convertToDatabaseColumn(ItemDetails attribute) {
            return PocketsConverter.convertToDatabaseColumn(attribute);
        }

        @Override
        public ItemDetails convertToEntityAttribute(String dbData) {
            return PocketsConverter.convertToEntityAttribute(dbData, ItemDetails.class);
        }
    }

    static class ProfileConfigDataConverter implements AttributeConverter<ProfileConfigData, String> {
        @Override
        public String convertToDatabaseColumn(ProfileConfigData attribute) {
            attribute.state = null; // don't save in DB
            return PocketsConverter.convertToDatabaseColumn(attribute);
        }

        @Override
        public ProfileConfigData convertToEntityAttribute(String dbData) {
            return PocketsConverter.convertToEntityAttribute(dbData, ProfileConfigData.class);
        }
    }

    static class PostingDataConverter implements AttributeConverter<List<Posting>, String> {
        @Override
        public String convertToDatabaseColumn(List<Posting> attribute) {
            return PocketsConverter.convertToDatabaseColumn(attribute);
        }

        @Override
        public List<Posting> convertToEntityAttribute(String dbData) {
            return PocketsConverter.convertToEntityAttribute(dbData, Posting.LIST_POSTING);
        }
    }
}
