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
public class ValidationError<T> {

    private final String objectName;
    private final String defaultMessage;

    @Nullable
    private final String field;

    @Nullable
    private final Object rejectedValue;

    @Nullable
    private final ConstraintViolation<T> underlyingViolation;

    /**
     * Create a new ValidationError instance.
     *
     * @param objectName     the name of the affected object
     * @param defaultMessage the default message to be used to resolve this message
     */
    public ValidationError(String objectName, String defaultMessage) {
        this(objectName, null, null, defaultMessage, null);
    }


    /**
     * Create a new ValidationError instance.
     *
     * @param violation the underlying underlyingViolation, coming from {@link javax.validation.Validator#validate(Object, Class[])}
     */
    public ValidationError(@NotNull ConstraintViolation<T> violation) {
        super();
        this.objectName = violation.getRootBean().getClass().getName();
        Assert.assertNotNull(objectName, "Object name must not be null");
        this.field = violation.getPropertyPath().toString();
        this.rejectedValue = violation.getInvalidValue();
        this.defaultMessage = violation.getMessage();
        this.underlyingViolation = violation;
    }

    /**
     * Create a new ValidationError instance.
     *
     * @param objectName     the name of the affected object
     * @param field          the affected field of the object
     * @param rejectedValue  the rejected field value
     * @param defaultMessage the default message to be used to resolve this message
     * @param violation      the underlying underlyingViolation, coming from {@link javax.validation.Validator#validate(Object, Class[])}
     */
    public ValidationError(String objectName, @Nullable String field, @Nullable Object rejectedValue, String defaultMessage, @Nullable ConstraintViolation<T> violation) {
        super();
        this.objectName = objectName;
        Assert.assertNotNull(objectName, "Object name must not be null");
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.defaultMessage = defaultMessage;
        this.underlyingViolation = violation;
    }


    /**
     * Return the affected field of the object.
     */
    @Nullable
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
    @Nullable
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
        ValidationError<T> otherError = (ValidationError<T>) other;
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
