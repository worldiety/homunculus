package org.homunculus.android.example.module.flow;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculusframework.factory.container.Binding;

public class FlowActivity extends HomunculusActivity {

    @Override
    protected Binding<?> create() {
        return new BindMySplash();
    }
}
