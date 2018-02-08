package org.homunculus.android.component.module.validator;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.homunculus.android.flavor.Resource;
import org.homunculusframework.annotations.Unfinished;
import org.homunculusframework.lang.Reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which is used to populate a model from a {@link View} or the other way around. It also offers a method
 * to set certain defined view-types to error-states.
 * <p>
 * Created by aerlemann on 05.02.18.
 */
@Unfinished
public class ModelViewPopulator<T> {

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

            if (field.getType() != String.class)
                continue;

            findObjectViewMatchRecursively(src, field, resource, dst, this::setViewValueToField);
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

            if (field.getType() != String.class)
                continue;

            findObjectViewMatchRecursively(dst, field, resource, src, this::setFieldValueToSpecificView);
        }
    }

    private void findObjectViewMatchRecursively(View dst, Field field, Resource resource, T src, OnMatchFound<T> onMatchFound) {
        if (dst instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) dst).getChildCount(); i++) {
                View found = dst.findViewById(resource.value());
                if (found != null) {
                    field.setAccessible(true);
                    onMatchFound.onMatchFound(found, field, src);
                    return;
                }
                //We did not find anything, try harder
                findObjectViewMatchRecursively(((ViewGroup) dst).getChildAt(i), field, resource, src, onMatchFound);
            }
        } else {
            if (dst.getId() == resource.value()) {
                field.setAccessible(true);
                onMatchFound.onMatchFound(dst, field, src);
            }
        }
    }

    private void setFieldValueToSpecificView(View dst, Field field, T src) {
        if (isTextInputLayout(dst)) {
            ((TextInputLayout) dst).getEditText().setText(getField(field, src));
        } else if (isEditText(dst)) {
            ((EditText) dst).setText(getField(field, src));
        }
    }

    private void setViewValueToField(View src, Field field, T dst) {
        if (isTextInputLayout(src)) {
            setField(((TextInputLayout) src).getEditText().getText().toString(), field, dst);
        } else if (isEditText(src)) {
            setField(((EditText) src).getText().toString(), field, dst);
        }
    }

    private boolean setErrorToView(View dst, String error) {
        if (isTextInputLayout(dst)) {
            ((TextInputLayout) dst).setError(error);
            return true;
        } else if (isEditText(dst)) {
            ((EditText) dst).setError(error);
            return true;
        }

        return false;
    }

    private CharSequence getField(Field field, T src) {
        try {
            return (CharSequence) field.get(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(String text, Field field, T dst) {
        try {
            field.set(dst, text);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEditText(View dst) {
        return dst instanceof EditText;
    }

    private boolean isTextInputLayout(View dst) {
        try {
            return dst instanceof TextInputLayout;
        } catch (NoClassDefFoundError e) {
            //dependency missing, so nothing to worry about
            return false;
        }
    }

    /**
     * Sets the given {@link View} or the Views in the given {@link ViewGroup} to an error-state, using the error-messages defined in the {@link BindingResult}.
     * <p>
     * Currently supported Views are: {@link EditText}, {@link TextInputLayout}
     *
     * @param dst    view or viewgroup, if view it's id is matched against the according field in dst
     * @param errors a {@link BindingResult} created by the {@link HomunculusValidator}
     * @return a List of {@link ValidationError<T>}s, which could not be set to a View in dst (either because unsupported, or because it is an error not created by
     * {@link HomunculusValidator}.
     */
    public List<ValidationError<T>> insertErrorState(View dst, BindingResult<T> errors) {
        List<ValidationError<T>> errorsWithNoMatchingView = new ArrayList<>();
        for (ValidationError<T> error : errors.getErrors()) {
            T model = error.getObject();
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

                if (field.getType() != String.class)
                    continue;

                findObjectViewMatchRecursively(dst, field, resource, model, (view, field1, object) -> {
                    if (!setErrorToView(view, error.getDefaultMessage())) {
                        errorsWithNoMatchingView.add(error);
                    }
                });
            }
        }

        return errorsWithNoMatchingView;
    }

    private interface OnMatchFound<T> {
        void onMatchFound(View view, Field field, T object);
    }
}
