package org.homunculus.android.example.module.validator;

import org.homunculus.android.component.module.validator.BindingResult;
import org.homunculus.android.component.module.validator.FieldSpecificValidationError;
import org.homunculus.android.component.module.validator.HomunculusValidator;
import org.homunculus.android.component.module.validator.UnspecificValidationError;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.lang.Panic;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Created by aerlemann on 05.02.18.
 */
@Singleton
public class TestValidatorController {

    @Inject
    HomunculusValidator validator;

    public ModelAndView save(ObjectToBeValidated entity) {
        BindingResult<ObjectToBeValidated> errors = validator.validate(entity);
        if (errors.hasErrors()) {
            //We already have errors. Don't do db stuff.
            return new BindValidatorUIS(entity, errors);
        }

        //check db stuff
        boolean dbFailedNotUniqueBlub = true;
        if (dbFailedNotUniqueBlub) {
            errors.addConstraintValidationError(new FieldSpecificValidationError<>(entity, "test1", "test1 must be unique", null));
        }

        try {
            if (true) {
                throw new SQLException("weired");
            }
        } catch (SQLException e) {
            errors.addCustomValidationError(new UnspecificValidationError("Versuchen Sie es später nochmal", e));
        }

        if (!errors.hasErrors()) {
            //return new ModelAndView("navigate:#backward"); //TODO implement logical directives
            throw new Panic("is this still required? => introduce NavigationBinding");
        } else {
            return new BindValidatorUIS(entity, errors);
        }


    }

}
