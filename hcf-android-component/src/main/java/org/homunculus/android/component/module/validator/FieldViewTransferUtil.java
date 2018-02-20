package org.homunculus.android.component.module.validator;

import android.view.View;

import org.homunculus.android.component.module.validator.conversionAdapters.ConversionAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

/**
 * Util class, which helps to set a value from a {@link Field} of a model of type M to a {@link View}, via reflection and using a {@link ConversionAdapter}.
 * <p>
 * Created by aerlemann on 19.02.18.
 */

class FieldViewTransferUtil<M> {

    /**
     * Transfers the value of a {@link Field} of a model M to a {@link View}, using a {@link ConversionAdapter}
     *
     * @param field             the {@link Field} to get the value from
     * @param view              the {@link View} to set the value to
     * @param model             the object, of which the field is a member
     * @param conversionAdapter the {@link ConversionAdapter} for the specific field-view combination
     */
    void transferFieldToView(Field field, View view, M model, ConversionAdapter conversionAdapter) {
        conversionAdapter.setFieldValueToView(getFieldValue(field, model), view);
    }

    /**
     * Transfers the value of a {@link View} to a {@link Field} of a model M, using a {@link ConversionAdapter}
     *
     * @param view              the {@link View} to get the value from
     * @param field             the {@link Field} to set the value to
     * @param model             the object, of which the field is a member
     * @param conversionAdapter the {@link ConversionAdapter} for the specific field-view combination
     */
    void transferViewToField(View view, Field field, M model, ConversionAdapter conversionAdapter) {
        setField(conversionAdapter.getFieldValueFromView(view), field, model);
    }

    /**
     * Checks, if a {@link ConversionAdapter} supports a given {@link Field}.
     *
     * @param field             the {@link Field}, which shall be supported by the {@link ConversionAdapter}
     * @param model             the object, of which the field is a member
     * @param conversionAdapter the {@link ConversionAdapter}, which shall support the {@link Field}
     * @return true, if the given {@link Field} is supported by the {@link ConversionAdapter}, else false
     */
    boolean isFieldTypeSupported(Field field, M model, ConversionAdapter conversionAdapter) {
        Object value = getFieldValue(field, model);
        Class valueClass;
        if (value == null)
            valueClass = field.getType();
        else
            valueClass = value.getClass();
        String className = getClassNameForGenericType(1, conversionAdapter);
        try {
            return valueClass != null && Class.forName(className).isAssignableFrom(valueClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks, if a {@link ConversionAdapter} supports a given {@link View}.
     *
     * @param view              the {@link View}, which shall be supported by the {@link ConversionAdapter}
     * @param conversionAdapter the {@link ConversionAdapter}, which shall support the {@link View}
     * @return true, if the given {@link View} is supported by the {@link ConversionAdapter}, else false
     */
    boolean isViewTypeSupported(View view, ConversionAdapter conversionAdapter) {
        try {
            String className = getClassNameForGenericType(0, conversionAdapter);
            return Class.forName(className).isAssignableFrom(view.getClass());
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private String getClassNameForGenericType(int nrOfArgumentInSignature, ConversionAdapter conversionAdapter) {
        ParameterizedType parameterizedType = ((ParameterizedType) conversionAdapter.getClass().getGenericInterfaces()[0]);
        if (parameterizedType.getActualTypeArguments().length != 2)
            throw new RuntimeException("Number of arguments in generic interface does not match ConversionAdapter(2)!");
        if (!(parameterizedType.getActualTypeArguments()[nrOfArgumentInSignature] instanceof Class))
            throw new RuntimeException("Argument " + nrOfArgumentInSignature + " is not of type Class!");
        return ((Class) parameterizedType.getActualTypeArguments()[nrOfArgumentInSignature]).getName();
    }

    private Object getFieldValue(Field field, M src) {
        try {
            return field.get(src);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object value, Field field, M dst) {
        try {
            field.set(dst, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
