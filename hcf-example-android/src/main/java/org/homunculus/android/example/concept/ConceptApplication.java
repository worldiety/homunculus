package org.homunculus.android.example.concept;

import org.homunculus.android.component.HomunculusApplication;
import org.homunculusframework.factory.flavor.hcf.ScopeElement;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class ConceptApplication extends HomunculusApplication<ConceptApplicationScope> {


    protected ConceptApplicationScope createScope() {
        return new ConceptApplicationScope(this);
    }

    @ScopeElement
    MyCustomDatabase createMyCustomDatabase(){
        return new MyCustomDatabase();
    }


}








