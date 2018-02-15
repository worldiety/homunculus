package org.homunculus.android.component.module.validator;

import org.homunculusframework.annotations.Unfinished;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Result, which wraps {@link ConstraintViolation} into the slimmer {@link ConstraintValidationError} and allows adding additional custom errors
 * <p>
 * Created by aerlemann on 05.02.18.
 */

@Unfinished
public class BindingResult<T> {

    private Set<ConstraintValidationError<T>> constraintValidationErrors;
    private Set<CustomValidationError> customValidationErrors;

    public BindingResult() {
        super();
    }

    public BindingResult(Set<ConstraintViolation<T>> violations) {
        super();
        constraintValidationErrors = new HashSet<>();
        customValidationErrors = new HashSet<>();

        for (ConstraintViolation<T> violation : violations) {
            constraintValidationErrors.add(new ConstraintValidationError<>(violation));
        }
    }

    public BindingResult(Set<ConstraintValidationError<T>> constraintValidationErrors, Set<CustomValidationError> customValidationErrors) {
        super();
        this.constraintValidationErrors = new HashSet<>(constraintValidationErrors);
        this.customValidationErrors = new HashSet<>(customValidationErrors);
    }

    public Set<ConstraintValidationError<T>> getConstraintValidationErrors() {
        return constraintValidationErrors;
    }

    public Set<CustomValidationError> getCustomValidationErrors() {
        return customValidationErrors;
    }

    public void addCustomValidationError(CustomValidationError error) {
        customValidationErrors.add(error);
    }

    public boolean hasConstraintValidationErrors() {
        return !constraintValidationErrors.isEmpty();
    }

    public boolean hasCustomValidationErrors() {
        return !customValidationErrors.isEmpty();
    }

    public boolean hasErrors() {
        return hasConstraintValidationErrors() || hasCustomValidationErrors();
    }
}
