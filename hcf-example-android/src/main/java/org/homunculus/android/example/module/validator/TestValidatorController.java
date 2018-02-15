package org.homunculus.android.example.module.validator;

import org.homunculus.android.component.module.validator.BindingResult;
import org.homunculus.android.component.module.validator.ConstraintValidationError;
import org.homunculus.android.component.module.validator.CustomValidationError;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculusframework.navigation.ModelAndView;

import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Created by aerlemann on 05.02.18.
 */
@Singleton
@Named("/validate")
public class TestValidatorController {

    @Named("/save")
    public ModelAndView save(ObjectToBeValidated entity, HomunculusValidator validator) {
        BindingResult<ObjectToBeValidated> errors = validator.validate(entity);

        //check db stuff
        boolean dbFailedNotUniqueBlub = true;
        if (dbFailedNotUniqueBlub) {
            errors.addCustomValidationError(new CustomValidationError("DB not unique blub", new RuntimeException()));
        }

        if (!errors.hasErrors()) {
            return new ModelAndView("navigate:#backward"); //TODO implement logical directives
        } else {
            return new ModelAndView("validator").put("viewModel", entity).put("errors", errors);
        }


    }

}
