package org.homunculus.android.component.module.validator;

import junit.framework.Assert;

import org.homunculusframework.annotations.Unfinished;
import org.springframework.util.ObjectUtils;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;

/**
 * Simple error-class offering easy access to the important parameters (for validation). If more details are needed, the underlying {@link ConstraintViolation} can
 * be accessed via {@link #getUnderlyingViolation()}.
 * <p>
 * Created by aerlemann on 05.02.18.
 */
@Unfinished
public class ConstraintValidationError<T> {

    private final String objectName;
    private final String defaultMessage;
    private final String field;

    @Nullable
    private final Object rejectedValue;
    private final ConstraintViolation<T> underlyingViolation;


    /**
     * Create a new ValidationError instance.
     *
     * @param violation the underlying underlyingViolation, coming from {@link javax.validation.Validator#validate(Object, Class[])}
     */
    public ConstraintValidationError(@NotNull ConstraintViolation<T> violation) {
        super();
        this.objectName = violation.getRootBean().getClass().getName();
        Assert.assertNotNull(objectName, "Object name must not be null");
        this.field = violation.getPropertyPath().toString();
        Assert.assertNotNull(field);
        this.rejectedValue = violation.getInvalidValue();
        this.defaultMessage = violation.getMessage();
        this.underlyingViolation = violation;
    }

    /**
     * Return the affected field of the object.
     */
    public String getField() {
        return this.field;
    }

    /**
     * Return the rejected field value.
     */
    @Nullable
    public Object getRejectedValue() {
        return this.rejectedValue;
    }

    /**
     * Return the name of the affected object.
     */
    public String getObjectName() {
        return this.objectName;
    }

    /**
     * Return the affected object.
     */
    public T getObject() {
        return this.underlyingViolation == null ? null : underlyingViolation.getRootBean();
    }

    /**
     * Returns the default error message
     *
     * @return
     */
    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    /**
     * Returns the underlying ConstraintViolation, coming from {@link javax.validation.Validator#validate(Object, Class[])} or null, if the error is not coming
     * from {@link javax.validation.Validator#validate(Object, Class[])}
     *
     * @return
     */
    public ConstraintViolation getUnderlyingViolation() {
        return this.underlyingViolation;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || other.getClass() != getClass() || !super.equals(other)) {
            return false;
        }
        ConstraintValidationError<T> otherError = (ConstraintValidationError<T>) other;
        return (ObjectUtils.nullSafeEquals(getField(), otherError.getField()) &&
                ObjectUtils.nullSafeEquals(getRejectedValue(), otherError.getRejectedValue()) &&
                ObjectUtils.nullSafeEquals(getObjectName(), otherError.getObjectName()) &&
                ObjectUtils.nullSafeEquals(getUnderlyingViolation(), otherError.getUnderlyingViolation()));
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode = 29 * hashCode + (getField() == null ? 1 : getField().hashCode());
        hashCode = 29 * hashCode + (getRejectedValue() == null ? 1 : getRejectedValue().hashCode());
        hashCode = 29 * hashCode + (getObjectName() == null ? 1 : getObjectName().hashCode());
        hashCode = 29 * hashCode + (getUnderlyingViolation() == null ? 1 : getUnderlyingViolation().hashCode());
        return hashCode;
    }

}
