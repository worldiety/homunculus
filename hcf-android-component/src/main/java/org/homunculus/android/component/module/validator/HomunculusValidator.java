package org.homunculus.android.component.module.validator;

import android.content.Context;

import org.homunculusframework.annotations.Unfinished;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import javax.inject.Singleton;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

/**
 * This class is basically just {@link Validator} initialized with a given configuration. It also wraps the result of {@link Validator#validate(Object, Class[])}
 * into a {@link BindingResult}.
 * <p>
 * Created by aerlemann on 05.02.18.
 */
@Unfinished
@Singleton
public class HomunculusValidator {
    private final Validator hibernateValidator;

    /**
     * Creates the default validator. Error messages from e.g. {@link org.hibernate.validator.constraints.NotEmpty} are returned as they are.
     *
     * @return a {@link HomunculusValidator} with the default configuration
     */
    public static HomunculusValidator createDefaultValidator() {
        return new HomunculusValidator(false, null);
    }

    /**
     * Creates a validator, which interpolates messages, so that the values defined in a strings.xml are used.
     * Error messages from e.g. {@link org.hibernate.validator.constraints.NotEmpty} are interpreted as string names defined in a strings.xml in the
     * Android resource directory. The address from the string name is resolved via {@link android.content.res.Resources#getIdentifier(String, String, String)}
     * and afterwards the localized string is returned via {@link Context#getString(int)}
     *
     * @return a {@link HomunculusValidator} with Android string configuration
     */
    public static HomunculusValidator createAndroidResourceMessagesValidator(Context context) {
        return new HomunculusValidator(true, context);
    }

    private HomunculusValidator(boolean buildForAndroidMessages, Context androidContext) {
        Validator validator;
        try {
            Configuration<?> validationConfig = Validation
                    .byDefaultProvider()
                    .configure()
                    .ignoreXmlConfiguration();

            if (buildForAndroidMessages) {
                validationConfig.messageInterpolator(new MessageInterpolator() {
                    @Override
                    public String interpolate(String messageTemplate, Context context) {
                        try {
                            int id = androidContext.getResources().getIdentifier(messageTemplate, "string", androidContext.getApplicationContext().getPackageName());
                            return androidContext.getString(id);
                        } catch (Exception e) {
                            LoggerFactory.getLogger(this.getClass()).info("Could not find resource: " + messageTemplate);
                            return messageTemplate;
                        }
                    }

                    @Override
                    public String interpolate(String messageTemplate, Context context, Locale locale) {
                        return interpolate(messageTemplate, context);
                    }
                });
            }

            ValidatorFactory validatorFactory = validationConfig.buildValidatorFactory();
            validator = validatorFactory.getValidator();
        } catch (ExceptionInInitializerError e) {
            LoggerFactory.getLogger(this.getClass()).error("Could not initialize HibernateValidator on this device! Validation via HibernateValidator will not work!", e);
            validator = new UnsupportedDeviceValidator();
        }

        hibernateValidator = validator;
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
