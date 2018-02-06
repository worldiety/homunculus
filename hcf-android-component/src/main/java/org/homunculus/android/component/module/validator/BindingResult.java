package org.homunculus.android.component.module.validator;

import org.homunculusframework.annotations.Unfinished;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Result, which wraps {@link ConstraintViolation} into the slimmer {@link ValidationError} and allows adding additional custom errors
 * <p>
 * Created by aerlemann on 05.02.18.
 */

@Unfinished
public class BindingResult<T> {

    private Set<ValidationError<T>> errors;

    public BindingResult() {
        super();
    }

    public BindingResult(Set<ConstraintViolation<T>> violations) {
        super();
        errors = new HashSet<>();

        for (ConstraintViolation<T> violation : violations) {
            errors.add(new ValidationError<>(violation));
        }
    }

    public Set<ValidationError<T>> getErrors() {
        return errors;
    }

    public void addError(ValidationError<T> error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
