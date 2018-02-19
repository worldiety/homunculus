package org.homunculus.android.example.module.validator;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.homunculus.android.component.module.validator.BindingResult;
import org.homunculus.android.component.module.validator.DefaultModelViewPopulator;
import org.homunculus.android.component.module.validator.FieldSpecificValidationError;
import org.homunculus.android.component.module.validator.UnspecificValidationError;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by aerlemann on 04.02.18.
 */
@Named("/validator")
public class ValidatorUIS extends FrameLayout {

    @Inject
    private Activity activity;

    @Inject
    Navigation navigation;

    @Inject
    @Named("viewModel")
    private ObjectToBeValidated viewModel;

    @Inject
    @Named("errors")
    private BindingResult<ObjectToBeValidated> errors;

    @Inject
    private DefaultModelViewPopulator<ObjectToBeValidated> modelViewPopulator;

    public ValidatorUIS(@NonNull Context context) {
        super(context);
    }

    @PostConstruct
    private void apply() {
        activity.setContentView(this);
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.activity_validator, null);
        addView(layout);

        if (viewModel == null) {
            viewModel = new ObjectToBeValidated();
        }
        modelViewPopulator.populateView(viewModel, layout);

        if (errors != null) {
            BindingResult<ObjectToBeValidated> errorResult = modelViewPopulator.insertErrorState(layout, errors);
            for (FieldSpecificValidationError<ObjectToBeValidated> error : errorResult.getFieldSpecificValidationErrors()) {
                //handle errors, which cannot be assigned to a View
                LoggerFactory.getLogger(this.getClass()).error("Handle me! I'm a constraint error!: " + error.getField() + ", " + error.getObjectName() + ", " + error.getRejectedValue() + ", " + error.getDefaultMessage());

            }

            for (UnspecificValidationError error : errorResult.getUnspecificValidationErrors()) {
                //handle custom errors
                LoggerFactory.getLogger(this.getClass()).error("Handle me! I'm a custom error!: " + error.getMessage() + ", " + error.getException());

            }
        }
        Button buttonValidate = layout.findViewById(R.id.bt_validate);

        buttonValidate.setOnClickListener(view -> {
            modelViewPopulator.populateBean(layout, viewModel);
            navigation.forward(new Request("validate/save").put("entity", viewModel));
        });
    }
}
