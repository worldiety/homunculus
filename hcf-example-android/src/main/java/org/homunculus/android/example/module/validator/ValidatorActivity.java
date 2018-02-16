package org.homunculus.android.example.module.validator;

import android.os.Bundle;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.factory.container.Request;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ValidatorActivity extends HomunculusActivity {

    @Override
    protected Request create() {
        return new Request(ValidatorSplash.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HomunculusValidator validator = HomunculusValidator.createAndroidResourceMessagesValidator(this);
        getScope().put("$validator", validator);
    }
}
