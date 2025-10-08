package com.mycalendar.dev.util;

import com.mycalendar.dev.exception.InvalidFieldException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeSafe {
    public static void validateSortBy(String sortBy, Class<?> entityClass) {
        if (sortBy == null) return;

        List<String> validFields = new ArrayList<>();

        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            validFields.addAll(
                    Arrays.stream(current.getDeclaredFields())
                            .map(Field::getName)
                            .toList()
            );
            current = current.getSuperclass();
        }

        if (!validFields.contains(sortBy)) {
            throw new InvalidFieldException(sortBy, validFields);
        }
    }
}
