package org.homunculus.android.component.module.validator;

import org.homunculusframework.annotations.Unfinished;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

/*
import jakarta.validation.ConstraintViolation;

 */


/**
 * Result, which wraps {@link ConstraintViolation} into the slimmer {@link FieldSpecificValidationError} and allows adding additional custom errors
 * <p>
 * Created by aerlemann on 05.02.18.
 */

@Unfinished
public class BindingResult<T> {

    private Set<FieldSpecificValidationError<T>> fieldSpecificValidationErrors;
    private Set<UnspecificValidationError> unspecificValidationErrors;

    public BindingResult() {
        super();
    }

    public BindingResult(Set<ConstraintViolation<T>> violations) {
        super();
        fieldSpecificValidationErrors = new HashSet<>();
        unspecificValidationErrors = new HashSet<>();

        for (ConstraintViolation<T> violation : violations) {
            fieldSpecificValidationErrors.add(new FieldSpecificValidationError<>(violation));
        }
    }

    public BindingResult(Set<FieldSpecificValidationError<T>> fieldSpecificValidationErrors, Set<UnspecificValidationError> unspecificValidationErrors) {
        super();
        this.fieldSpecificValidationErrors = new HashSet<>(fieldSpecificValidationErrors);
        this.unspecificValidationErrors = new HashSet<>(unspecificValidationErrors);
    }

    public Set<FieldSpecificValidationError<T>> getFieldSpecificValidationErrors() {
        return fieldSpecificValidationErrors;
    }

    public Set<UnspecificValidationError> getUnspecificValidationErrors() {
        return unspecificValidationErrors;
    }

    public void addConstraintValidationError(FieldSpecificValidationError<T> fieldSpecificValidationError) {
        this.fieldSpecificValidationErrors.add(fieldSpecificValidationError);
    }

    public void addCustomValidationError(UnspecificValidationError error) {
        this.unspecificValidationErrors.add(error);
    }

    public boolean hasConstraintValidationErrors() {
        return !fieldSpecificValidationErrors.isEmpty();
    }

    public boolean hasCustomValidationErrors() {
        return !unspecificValidationErrors.isEmpty();
    }

    public boolean hasErrors() {
        return hasConstraintValidationErrors() || hasCustomValidationErrors();
    }
}
