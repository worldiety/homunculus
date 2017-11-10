package org.homunculus.android.example.module.flow;

import org.homunculus.android.component.HomunculusActivity;
import org.homunculusframework.factory.container.Request;

public class FlowActivity extends HomunculusActivity {

    @Override
    protected Request create() {
        return new Request(MySplash.class);
    }
}
