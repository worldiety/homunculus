package org.homunculus.android.component.module.validator;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.homunculus.android.component.module.validator.fieldValueAdapters.IntegerFieldValueAdapter;
import org.homunculus.android.component.module.validator.fieldValueAdapters.StringFieldValueAdapter;
import org.homunculus.android.component.module.validator.validatorViewConnectors.EditTextValidatorViewConnector;
import org.homunculus.android.component.module.validator.validatorViewConnectors.SpinnerValidatorViewConnector;
import org.homunculus.android.component.module.validator.validatorViewConnectors.TextInputLayoutValidatorViewConnector;
import org.homunculus.android.flavor.Resource;
import org.homunculusframework.annotations.Unfinished;
import org.homunculusframework.lang.Reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class which is used to populate a model from a {@link View} or the other way around. It also offers a method
 * to set certain defined view-types to error-states.
 * <p>
 * Created by aerlemann on 05.02.18.
 */
@Unfinished
public class ModelViewPopulator<T> {

    private List<ValidatorViewConnector<T>> validatorViewConnectors;
    private List<FieldValueAdapter<T>> fieldValueAdapters;

    public ModelViewPopulator() {
        validatorViewConnectors = new ArrayList<>();
        validatorViewConnectors.add(new TextInputLayoutValidatorViewConnector<>());
        validatorViewConnectors.add(new EditTextValidatorViewConnector<>());
        validatorViewConnectors.add(new SpinnerValidatorViewConnector<>());

        fieldValueAdapters = new ArrayList<>();
        fieldValueAdapters.add(new StringFieldValueAdapter<>());
        fieldValueAdapters.add(new IntegerFieldValueAdapter<>());
    }

    /**
     * Adds an additional {@link ValidatorViewConnector<T>}
     *
     * @param validatorViewConnector a {@link ValidatorViewConnector<T>}
     */
    public void addValidatorViewConnector(ValidatorViewConnector<T> validatorViewConnector) {
        validatorViewConnectors.add(validatorViewConnector);
    }

    /**
     * Adds an additional {@link FieldValueAdapter<T>}
     *
     * @param fieldValueAdapter a {@link FieldValueAdapter<T>}
     */
    public void addFieldValueAdapter(FieldValueAdapter<T> fieldValueAdapter) {
        fieldValueAdapters.add(fieldValueAdapter);
    }

    /**
     * Populates a given model with the values of a {@link View}. Matching fields between model and view are found via reflection
     * using the {@link Resource}-Annotation in the model and the {@link View#getId()}-method in the view.
     * <p>
     * Currently supported Views are: {@link EditText}, {@link TextInputLayout}
     *
     * @param src view or viewgroup, if view it's id is matched against the according field in dst
     * @param dst the bean to be filled
     */
    public void populateBean(View src, T dst) {
        for (Field field : Reflection.getFields(dst.getClass())) {
            Resource resource = field.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            field.setAccessible(true);

            FieldValueAdapter<T> fieldValueAdapter = getFieldValueConnector(field, dst);
            if (fieldValueAdapter == null)
                throw new RuntimeException("Unsupported field type!: " + field.getType());

            findObjectViewMatchRecursively(src, field, resource, dst, (view, field1, object) -> setViewValueToField(view, field1, object, fieldValueAdapter));
        }
    }

    /**
     * Populates a {@link View} with the values of a given model. Matching fields between model and view are found via reflection
     * using the {@link Resource}-Annotation in the model and the {@link View#getId()}-method in the view.
     * <p>
     * Currently supported Views are: {@link EditText}, {@link TextInputLayout}
     *
     * @param src the bean
     * @param dst view or viewgroup, if view it's id is matched against the according field in dst
     */
    public void populateView(T src, View dst) {
        for (Field field : Reflection.getFields(src.getClass())) {
            Resource resource = field.getAnnotation(Resource.class);
            if (resource == null)
                continue;

            field.setAccessible(true);

            FieldValueAdapter<T> fieldValueAdapter = getFieldValueConnector(field, src);
            if (fieldValueAdapter == null)
                throw new RuntimeException("Unsupported field type!: " + field.getType());

            findObjectViewMatchRecursively(dst, field, resource, src, (view, field1, object) -> setFieldValueToSpecificView(view, field1, object, fieldValueAdapter));
        }
    }

    private void findObjectViewMatchRecursively(View dst, Field field, Resource resource, T src, OnMatchFound<T> onMatchFound) {
        if (dst instanceof ViewGroup) {
            View found = dst.findViewById(resource.value());
            if (found != null) {
                onMatchFound.onMatchFound(found, field, src);
            }
        } else {
            if (dst.getId() == resource.value()) {
                onMatchFound.onMatchFound(dst, field, src);
            }
        }
    }

    private void setFieldValueToSpecificView(View dst, Field field, T src, FieldValueAdapter<T> fieldValueAdapter) {
        for (ValidatorViewConnector<T> validatorViewConnector : validatorViewConnectors) {
            if (validatorViewConnector.isViewOfThisKind(dst)) {
                validatorViewConnector.setFieldValueToSpecificView(dst, field, src, fieldValueAdapter);
                return;
            }
        }
    }

    private void setViewValueToField(View src, Field field, T dst, FieldValueAdapter<T> fieldValueAdapter) {
        for (ValidatorViewConnector<T> validatorViewConnector : validatorViewConnectors) {
            if (validatorViewConnector.isViewOfThisKind(src)) {
                validatorViewConnector.setViewValueToField(src, field, dst, fieldValueAdapter);
                return;
            }
        }
    }

    private boolean setErrorToView(View dst, String error, FieldValueAdapter<T> fieldValueAdapter) {
        for (ValidatorViewConnector<T> validatorViewConnector : validatorViewConnectors) {
            if (validatorViewConnector.isViewOfThisKind(dst)) {
                validatorViewConnector.setErrorToView(dst, error, fieldValueAdapter);
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the given {@link View} or the Views in the given {@link ViewGroup} to an error-state, using the error-messages defined in the {@link BindingResult}.
     * <p>
     * Currently supported Views are: {@link EditText}, {@link TextInputLayout}
     *
     * @param dst    view or viewgroup, if view it's id is matched against the according field in dst
     * @param errors a {@link BindingResult} created by the {@link HomunculusValidator}
     * @return a {@link BindingResult <T>} with errors, which could not be set to a View in dst (either because unsupported, or because it is an error not created by
     * {@link HomunculusValidator}.
     */
    public BindingResult<T> insertErrorState(View dst, BindingResult<T> errors) {
        Set<FieldSpecificValidationError<T>> errorsWithNoMatchingView = new HashSet<>();
        for (FieldSpecificValidationError<T> error : errors.getFieldSpecificValidationErrors()) {
            T model = error.getFieldParent();
            if (model == null || error.getField() == null) {
                errorsWithNoMatchingView.add(error);
                continue;
            }

            for (Field field : Reflection.getFields(model.getClass())) {
                if (!field.getName().equals(error.getField()))
                    continue;

                Resource resource = field.getAnnotation(Resource.class);
                if (resource == null)
                    continue;

                field.setAccessible(true);

                FieldValueAdapter<T> fieldValueAdapter = getFieldValueConnector(field, model);
                if (fieldValueAdapter == null)
                    throw new RuntimeException("Unsupported field type!: " + field.getType());

                findObjectViewMatchRecursively(dst, field, resource, model, (view, field1, object) -> {
                    if (!setErrorToView(view, error.getDefaultMessage(), fieldValueAdapter)) {
                        errorsWithNoMatchingView.add(error);
                    }
                });
            }
        }

        return new BindingResult<T>(errorsWithNoMatchingView, errors.getUnspecificValidationErrors());
    }

    private FieldValueAdapter<T> getFieldValueConnector(Field field, T object) {
        for (FieldValueAdapter<T> fieldValueAdapter : fieldValueAdapters) {
            if (fieldValueAdapter.isFieldTypeSupported(field, object)) {
                return fieldValueAdapter;
            }
        }
        return null;
    }

    private interface OnMatchFound<T> {
        void onMatchFound(View view, Field field, T object);
    }
}
