package org.homunculus.android.example.concept;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.HomunculusActivity;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.navigation.Navigation;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ConceptActivity extends HomunculusActivity<ConceptActivityScope> {


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

}
