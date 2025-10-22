package com.example.repository;

import java.beans.IntrospectionException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import jakarta.persistence.EntityNotFoundException; 
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface BaseRepository<T, U> extends JpaRepository<T, U> {
    
 
    @Transactional
    default T updateValues(U id, T updatedEntity) throws EntityNotFoundException {
        T existingEntity = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));

        copyNonNullProperties(updatedEntity, existingEntity);

        return save(existingEntity);
    }

    private void copyNonNullProperties(T source, T target) {
        try {
            for (java.beans.PropertyDescriptor pd : java.beans.Introspector.getBeanInfo(source.getClass(), Object.class).getPropertyDescriptors()) {
                try {
                    Object value = pd.getReadMethod().invoke(source);
                    if (value != null && pd.getWriteMethod() != null) {
                        pd.getWriteMethod().invoke(target, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to copy properties", e);
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException("Failed to copy properties", e);
        }
    }
}
