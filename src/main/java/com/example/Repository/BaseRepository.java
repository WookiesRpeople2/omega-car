package com.example.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.Configuration.SafeExecutor;

import jakarta.persistence.EntityNotFoundException;

import java.lang.reflect.Field;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    default Optional<T> deleteAndReturn(ID id) {
        Optional<T> entity =  findById(id);
        entity.ifPresent(this::delete);
        return entity;
    }

    default T updateValues(ID id, T updatedEntity) throws IllegalAccessException, EntityNotFoundException {
        Optional<T> existingOpt = findById(id);

        T existingEntity = existingOpt.get();

        for (Field field : updatedEntity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object newValue = field.get(updatedEntity);
            if (newValue != null) {
                field.set(existingEntity, newValue);
            }
        }
        T savedEntity = save(existingEntity);
        return savedEntity;
    }
}
