package org.homunculus.android.component.module.validator;

import org.homunculusframework.annotations.Unfinished;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

/**
 * This class is basically just {@link Validator} initialized with a default-configuration. It also wraps the result of {@link Validator#validate(Object, Class[])}
 * into a {@link BindingResult}.
 *
 * Created by aerlemann on 05.02.18.
 */
@Unfinished
@Singleton
public class HomunculusValidator {
    private final Validator hibernateValidator;

    public HomunculusValidator() {
        ValidatorFactory validatorFactory = Validation
                .byDefaultProvider()
                .configure()
                .ignoreXmlConfiguration()
                .buildValidatorFactory();

        hibernateValidator = validatorFactory.getValidator();
    }

    /**
     * Uses {@link Validator#validate(Object, Class[])} to create a set of {@link BindingResult}s, a simpler versions of {@link ConstraintViolation}
     *
     * @param object object to validate
     * @param groups the group or list of groups targeted for validation (defaults to
     *               {@link Default})
     * @return binding results or an empty set if none
     * @throws IllegalArgumentException if object is {@code null}
     *                                  or if {@code null} is passed to the varargs groups
     * @throws ValidationException      if a non recoverable error happens
     *                                  during the validation process
     */
    public <T> BindingResult<T> validate(T object, Class<?>... groups) {
        return new BindingResult<>(hibernateValidator.validate(object, groups));
    }
}
