package com.example.Configuration;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.example.Errors;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

public abstract class SafeExecutor {
  
    @FunctionalInterface
    private interface ThrowableSupplier<R> {
        R get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowableBiFunction<T, U, R> {
      R apply(T t, U u) throws Exception;
    }

    public static <T> T executeSafely(Supplier<T> action) {
        return handleExceptions(() -> action.get(), null);
    }

    public static <T, R> R executeSafely(Function<T, R> function, T arg) {
        String name = getReturnTypeName(function);
        return handleExceptions(() -> function.apply(arg), name);
    }

    public static <T, U, R> R executeSafely(ThrowableBiFunction<T, U, R> function, T arg1, U arg2) {
        return handleExceptions(() -> function.apply(arg1, arg2), null);
    }

    private static <R> R handleExceptions(ThrowableSupplier<R> supplier, String entityName) {
        try {
            return supplier.get();
        } catch (EntityExistsException e) {
            throw new EntityExistsException(
                entityName != null ? Errors.ALREADY_EXSISTS.format(entityName) : e.getMessage()
            );
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException(
                entityName != null ? Errors.NOT_FOUND.format(entityName + "s") : e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                Errors.ILLEGAL_EXCEPTION.format("UUID"), e
            );
        } catch (Exception e) {
            throw new RuntimeException(Errors.UNKNOWN_ERROR.getMessage(), e);
        }
    }

    private static <T, R> String getReturnTypeName(Function<T, R> function) {
      try {
          Method method = function.getClass().getDeclaredMethod("apply", Object.class);
          return method.getReturnType().getSimpleName();
      } catch (NoSuchMethodException e) {
          return "Entity";
      }
    }
}


