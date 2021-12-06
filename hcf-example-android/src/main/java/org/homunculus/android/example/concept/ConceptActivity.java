package org.homunculus.android.example.concept;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;




import androidx.core.app.ActivityCompat;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ConceptActivity extends HomunculusActivity<ConceptActivityScope> {
    @Override
    protected ConceptActivityScope createScope() {
        return null;
    }

    @Override
    protected Binding<?, ?> create() {
        return null;
    }

/*
    @Override
    protected ConceptActivityScope createScope() {
        return new ConceptActivityScope(((ConceptApplication) getApplication()).getScope(), this);
    }

    @Override
    protected Binding<?, ?> create() {
        return new BindUISA();
    }

    @ScopeElement
    HomunculusValidator createValidator(){
        return HomunculusValidator.createAndroidResourceMessagesValidator(this);
    }

 */

}


