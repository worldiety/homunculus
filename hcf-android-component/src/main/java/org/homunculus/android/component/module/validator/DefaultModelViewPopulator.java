package org.homunculus.android.component.module.validator;

import org.homunculus.android.component.module.validator.conversionAdapters.DoubleToEditTextAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.DoubleToTextInputLayoutAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.FloatToEditTextAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.FloatToTextInputLayoutAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.IntegerToEditTextAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.IntegerToTextInputLayoutAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToEditTextAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToSpinnerAdapter;
import org.homunculus.android.component.module.validator.conversionAdapters.StringToTextInputLayoutAdapter;

/**
 * ModelViewPopulator T with default conversion adapters
 * <p>
 * Created by aerlemann on 19.02.18.
 */
public class DefaultModelViewPopulator<T> extends ModelViewPopulator<T> {

    public DefaultModelViewPopulator() {
        super();
        addConversionAdapter(new StringToTextInputLayoutAdapter());
        addConversionAdapter(new StringToEditTextAdapter());
        addConversionAdapter(new StringToSpinnerAdapter());

        addConversionAdapter(new IntegerToTextInputLayoutAdapter());
        addConversionAdapter(new IntegerToEditTextAdapter());

        addConversionAdapter(new FloatToTextInputLayoutAdapter());
        addConversionAdapter(new FloatToEditTextAdapter());

        addConversionAdapter(new DoubleToTextInputLayoutAdapter());
        addConversionAdapter(new DoubleToEditTextAdapter());
    }
}
