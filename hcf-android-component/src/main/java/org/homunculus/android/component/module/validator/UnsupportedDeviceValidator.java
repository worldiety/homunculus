package org.homunculus.android.component.module.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

/**
 * Some devices are not supported by {@link org.hibernate.validator.HibernateValidator}. This is a mock implementation for unsupported devices.
 *
 * Created by aerlemann on 07.03.18.
 */

public class UnsupportedDeviceValidator implements Validator, ExecutableValidator {
    @Override
    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return null;
    }

    @Override
    public ExecutableValidator forExecutables() {
        return this;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateParameters(T object, Method method, Object[] parameterValues, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorParameters(Constructor<? extends T> constructor, Object[] parameterValues, Class<?>[] groups) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(Constructor<? extends T> constructor, T createdObject, Class<?>[] groups) {
        return new HashSet<>();
    }
}
