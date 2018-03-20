package org.homunculus.android.component.module.toolbarbuilder.templates;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.toolbarbuilder.ToolbarConfiguration;
import org.homunculusframework.factory.scope.Scope;

/**
 * Created by aerlemann on 19.03.18.
 */

public class DefaultToolbarConfiguration extends ToolbarConfiguration {

    public DefaultToolbarConfiguration(EventAppCompatActivity activity, Scope scope) {
        super(activity, scope);
    }

    @Override
    protected void configure() {
        //nothing to do here in default configuration
    }
}
