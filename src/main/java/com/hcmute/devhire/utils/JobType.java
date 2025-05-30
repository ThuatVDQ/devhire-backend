package com.hcmute.devhire.utils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobType {
    PART_TIME("PART_TIME"),
    FULL_TIME("FULL_TIME"),
    INTERNSHIP("INTERNSHIP");

    private final String value;

    JobType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static JobType fromValue(String value) {
        for (JobType type : JobType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid JobType: " + value);
    }
}