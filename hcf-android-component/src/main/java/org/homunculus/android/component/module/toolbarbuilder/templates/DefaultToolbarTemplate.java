package org.homunculus.android.component.module.toolbarbuilder.templates;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.toolbarbuilder.ToolbarTemplate;
import org.homunculusframework.factory.scope.Scope;

/**
 * Created by aerlemann on 19.03.18.
 */

public class DefaultToolbarTemplate extends ToolbarTemplate {

    public DefaultToolbarTemplate(EventAppCompatActivity activity, Scope scope) {
        super(activity, scope);
    }

    @Override
    protected void configureTemplate(ToolbarTemplate toolbarTemplate) {
        //nothing to do here in default configuration
    }
}
