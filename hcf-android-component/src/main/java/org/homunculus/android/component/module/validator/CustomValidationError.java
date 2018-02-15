package org.homunculus.android.component.module.validator;

import org.homunculusframework.annotations.Unfinished;

import javax.annotation.Nullable;

/**
 * Simple error class for custom errors, which can be added to a {@link BindingResult}
 * <p>
 * Created by aerlemann on 15.02.18.
 */
@Unfinished
public class CustomValidationError {

    private String message;
    private Exception exception;

    /**
     * Constructor, which accepts an error message, which may later be shown and the cause {@link Exception}
     *
     * @param message   a error message, which may be shown somewhere in the UI later
     * @param exception the exception, which is reason for this error
     */
    public CustomValidationError(String message, Exception exception) {
        this.message = message;
        this.exception = exception;
    }

    /**
     * Constructor, which accepts only an error message, which may later be shown. May be used, if there is no {@link Exception}
     *
     * @param message a error message, which may be shown somewhere in the UI later
     */
    public CustomValidationError(String message) {
        this.message = message;
        this.exception = null;
    }

    /**
     * Constructor, which accepts only an {@link Exception}, which was cause of the error
     *
     * @param exception the exception, which is reason for this error
     */
    public CustomValidationError(Exception exception) {
        this.message = null;
        this.exception = exception;
    }

    public boolean hasMessage() {
        return message != null;
    }

    public boolean hasException() {
        return exception != null;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}
