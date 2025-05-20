package com.mycalendar.dev.util;


import org.modelmapper.ModelMapper;

public class EntityMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static <T, U> U mapToEntity(T request, Class<U> entityClass) {
        return modelMapper.map(request, entityClass);
    }
}
