package org.homunculus.android.example.module.validator;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.homunculus.android.component.module.validator.BindingResult;
import org.homunculus.android.component.module.validator.ModelViewPopulator;
import org.homunculus.android.component.module.validator.ValidationError;
import org.homunculus.android.example.R;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    private ModelViewPopulator<ObjectToBeValidated> modelViewPopulator;

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
            List<ValidationError<ObjectToBeValidated>> errorsWhichCouldNotBeInserted = modelViewPopulator.insertErrorState(layout, errors);
            for (ValidationError<ObjectToBeValidated> error : errorsWhichCouldNotBeInserted) {
                //TODO handle errors, which cannot be assigned to a View
                LoggerFactory.getLogger(this.getClass()).error("Handle me! I'm an error!: " + error.getField() + ", " + error.getObjectName() + ", " + error.getRejectedValue() + ", " + error.getDefaultMessage());

            }
        }
        Button buttonValidate = layout.findViewById(R.id.bt_validate);

        buttonValidate.setOnClickListener(view -> {
            modelViewPopulator.populateBean(layout, viewModel);
            navigation.forward(new Request("validate/save").put("entity", viewModel));
        });
    }
}
