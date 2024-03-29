package org.homunculus.android.example.concept;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ASecondActivity extends HomunculusActivity<ASecondActivityScope> {


    @Override
    protected ASecondActivityScope createScope() {
        return new ASecondActivityScope(((ConceptApplication) getApplication()).getScope(), this);
    }

    @Override
    protected Binding<?, ?> create() {
        return null;
    }

    @ScopeElement
    HomunculusValidator createValidator(){
        return HomunculusValidator.createAndroidResourceMessagesValidator(this);
    }

}

