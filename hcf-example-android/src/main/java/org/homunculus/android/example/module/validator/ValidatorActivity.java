package org.homunculus.android.example.module.validator;

import android.os.Bundle;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.factory.container.Binding;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ValidatorActivity extends HomunculusActivity {

    @Override
    protected Binding<?> create() {
        return new BindValidatorUIS(null, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HomunculusValidator validator = HomunculusValidator.createAndroidResourceMessagesValidator(this);
        getScope().put("$validator", validator);
    }
}
