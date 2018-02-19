package org.homunculus.android.component.module.validator;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.homunculus.android.component.module.validator.conversionAdapters.ConversionAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToEditTextAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToSpinnerAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToTextInputLayoutAdapter;
import org.homunculus.android.flavor.Resource;
import org.homunculusframework.annotations.Unfinished;
import org.homunculusframework.lang.Reflection;
import org.slf4j.LoggerFactory;

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

    private List<ConversionAdapter> conversionAdapters;
    private FieldViewTransferUtil<T> fieldViewTransferUtil;

    public ModelViewPopulator() {
        conversionAdapters = new ArrayList<>();
        conversionAdapters.add(new StringToTextInputLayoutAdapter<T>());
        conversionAdapters.add(new StringToEditTextAdapter<T>());
        conversionAdapters.add(new StringToSpinnerAdapter<T>());

        fieldViewTransferUtil = new FieldViewTransferUtil<>();
    }

    /**
     * Adds an additional {@link ConversionAdapter}
     *
     * @param conversionAdapter a {@link ConversionAdapter}
     */
    public void addConversionAdapter(ConversionAdapter conversionAdapter) {
        conversionAdapters.add(conversionAdapter);
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
            findObjectViewMatchRecursively(src, field, resource, dst, (view, field1, object) -> setViewValueToField(view, field1, object, getConversionAdapter(field1, object, view)));
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
            findObjectViewMatchRecursively(dst, field, resource, src, (view, field1, object) -> setFieldValueToSpecificView(view, field1, object, getConversionAdapter(field1, object, view)));
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

    private void setFieldValueToSpecificView(View dst, Field field, T src, ConversionAdapter conversionAdapter) {
        if (conversionAdapter != null) {
            fieldViewTransferUtil.transferFieldToView(field, dst, src, conversionAdapter);
        }
    }

    private void setViewValueToField(View src, Field field, T dst, ConversionAdapter conversionAdapter) {
        if (conversionAdapter != null) {
            fieldViewTransferUtil.transferViewToField(src, field, dst, conversionAdapter);
        }
    }

    private boolean setErrorToView(View dst, String error, ConversionAdapter conversionAdapter) {
        if (conversionAdapter != null) {
            conversionAdapter.getErrorHandler().setErrorToView(dst, error);
            return true;
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
                findObjectViewMatchRecursively(dst, field, resource, model, (view, field1, object) -> {
                    if (!setErrorToView(view, error.getDefaultMessage(), getConversionAdapter(field1, object, view))) {
                        errorsWithNoMatchingView.add(error);
                    }
                });
            }
        }

        return new BindingResult<T>(errorsWithNoMatchingView, errors.getUnspecificValidationErrors());
    }

    private ConversionAdapter getConversionAdapter(Field field, T object, View view) {
        for (ConversionAdapter conversionAdapter : conversionAdapters) {
            if (fieldViewTransferUtil.isFieldTypeSupported(field, object, conversionAdapter)) {
                if (fieldViewTransferUtil.isViewTypeSupported(view, conversionAdapter)) {
                    return conversionAdapter;
                }
            }
        }

        LoggerFactory.getLogger(this.getClass()).warn("Could not find ConversionAdapter for view-field combination: " + view.getClass().getSimpleName() + "-" + field.getType().getName());

        return null;
    }

    private interface OnMatchFound<T> {
        void onMatchFound(View view, Field field, T object);
    }
}
