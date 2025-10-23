package com.example.controller;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseController<T, U> {

    private final Class<T> entityClass;
    private final Class<U> dtoClass;

    protected BaseController() {
        Type superClass = getClass().getGenericSuperclass();

        if (!(superClass instanceof ParameterizedType)) {
            throw new RuntimeException("BaseController must be parameterized with <T, U>");
        }

        ParameterizedType parameterized = (ParameterizedType) superClass;
        Type[] typeArgs = parameterized.getActualTypeArguments();

        this.entityClass = extractClass(typeArgs[0]);
        this.dtoClass = extractClass(typeArgs[1]);
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
                    (m.getName().startsWith("is") && m.getParameterCount() == 0
                            && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class))) {
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
            if (onlyNonNull && value == null){
                continue;
            }
            Method setter = targetSetters.get(key);
            if (setter == null){
                continue;
            }
            Class<?> paramType = setter.getParameterTypes()[0];
            if (value == null) {
                setter.invoke(target, value);
                continue;
            }
            Class<?> valueType = value.getClass();
            boolean assignable = paramType.isAssignableFrom(valueType);
            if (!assignable && paramType.isPrimitive()) {
                Class<?> wrapper = primitiveToWrapper(paramType);
                assignable = wrapper != null && wrapper.isAssignableFrom(valueType);
            }
            if (assignable) {
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
        return name.replace("_", "").toLowerCase(Locale.ROOT);
    }

    private static Class<?> primitiveToWrapper(Class<?> primitive) {
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == short.class) return Short.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        if (primitive == double.class) return Double.class;
        if (primitive == char.class) return Character.class;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <X> Class<X> extractClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<X>) type;
        } else if (type instanceof ParameterizedType p) {
            return (Class<X>) p.getRawType();
        } else {
            throw new RuntimeException("Cannot determine class from type: " + type);
        }
    }
}