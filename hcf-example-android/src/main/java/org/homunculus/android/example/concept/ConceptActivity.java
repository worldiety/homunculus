package org.homunculus.android.example.concept;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.navigation.Navigation;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ConceptActivity extends EventAppCompatActivity {

    private ConceptActivityScope scope;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scope = new ConceptActivityScope(((ConceptApplication) getApplication()).getScope(), this);
    }

    @ScopeElement
    Navigation createNavigation() {
        return null;
    }
}
