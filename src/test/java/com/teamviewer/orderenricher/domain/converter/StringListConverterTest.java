package com.teamviewer.orderenricher.domain.converter;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

class StringListConverterTest {

    private final StringListConverter converter = new StringListConverter();

    @Test
    void convertToDatabaseColumn_withValidList_returnsJoinedString() {
        List<String> list = List.of("tag1", "tag2", "tag3");
        String result = converter.convertToDatabaseColumn(list);
        assertThat(result).isEqualTo("tag1,tag2,tag3");
    }

    @Test
    void convertToDatabaseColumn_withNullList_returnsNull() {
        String result = converter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
    }

    @Test
    void convertToDatabaseColumn_withEmptyList_returnsNull() {
        String result = converter.convertToDatabaseColumn(Collections.emptyList());
        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_withValidString_returnsList() {
        String dbData = "tag1,tag2,tag3";
        List<String> result = converter.convertToEntityAttribute(dbData);
        assertThat(result).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    void convertToEntityAttribute_withNullString_returnsEmptyList() {
        List<String> result = converter.convertToEntityAttribute(null);
        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void convertToEntityAttribute_withEmptyString_returnsEmptyList() {
        List<String> result = converter.convertToEntityAttribute(" ");
        assertThat(result).isNotNull().isEmpty();
    }
}
