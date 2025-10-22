package com.example.Controller;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseController<T, ID> {

    private final Class<T> entityClass;
    private final Class<?> dtoClass;

    protected BaseController(Class<T> entityClass, Class<?> dtoClass) {
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    protected <E, D> D toDto(E entity) {
        try {
            @SuppressWarnings("unchecked")
            Class<D> cls = (Class<D>) this.dtoClass;
            D dto = cls.getDeclaredConstructor().newInstance();
            copyProperties(entity, dto, false);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map entity to DTO", e);
        }
    }

    protected <D> T fromDtoPartial(D dto) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            copyProperties(dto, entity, true);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map DTO to entity", e);
        }
    }

    private static void copyProperties(Object source, Object target, boolean onlyNonNull) throws Exception {
        Map<String, Method> sourceGetters = new HashMap<>();
        for (Method m : source.getClass().getMethods()) {
            if ((m.getName().startsWith("get") && m.getParameterCount() == 0) ||
                (m.getName().startsWith("is") && m.getParameterCount() == 0 && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class))) {
                String prop = propertyNameFromAccessor(m.getName());
                sourceGetters.put(normalize(prop), m);
            }
        }

        Map<String, Method> targetSetters = new HashMap<>();
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                String prop = m.getName().substring(3);
                targetSetters.put(normalize(prop), m);
            }
        }

        for (Map.Entry<String, Method> entry : sourceGetters.entrySet()) {
            String key = entry.getKey();
            Method getter = entry.getValue();
            Object value = getter.invoke(source);
            if (onlyNonNull && value == null) continue;
            Method setter = targetSetters.get(key);
            if (setter == null) continue;
            Class<?> paramType = setter.getParameterTypes()[0];
            if (value == null || paramType.isAssignableFrom(value.getClass())) {
                setter.invoke(target, value);
            }
        }
    }

    private static String propertyNameFromAccessor(String accessorName) {
        if (accessorName.startsWith("get")) {
            return accessorName.substring(3);
        }
        if (accessorName.startsWith("is")) {
            return accessorName.substring(2);
        }
        return accessorName;
    }

    private static String normalize(String name) {
        // remove underscores and lowercase for lenient matching between snake_case and camelCase
        return name.replace("_", "").toLowerCase(Locale.ROOT);
    }
}