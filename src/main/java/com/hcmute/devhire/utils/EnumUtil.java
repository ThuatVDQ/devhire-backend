package com.hcmute.devhire.utils;

public class EnumUtil {

    public static <T extends Enum<T>> T getEnumFromString(Class<T> enumClass, String value) {
        if (enumClass != null && value != null) {
            try {
                return Enum.valueOf(enumClass, value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid value for enum " + enumClass.getSimpleName() + ": " + value);
            }
        }
        return null;
    }
}
