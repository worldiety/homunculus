package org.homunculus.android.example.concept;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.HomunculusActivity;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.navigation.Navigation;

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


}
