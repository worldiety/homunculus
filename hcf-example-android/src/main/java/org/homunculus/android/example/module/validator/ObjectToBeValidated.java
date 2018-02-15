package org.homunculus.android.example.module.validator;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.homunculus.android.example.R;
import org.homunculus.android.flavor.Resource;

import java.io.Serializable;

import javax.validation.constraints.Size;

/**
 * Created by aerlemann on 04.02.18.
 */

public class ObjectToBeValidated implements Serializable {

    @Resource(R.id.ed_test1)
    @Size(min = 8, message = "error_min_eight_chars")
    private String test1 = "";

    @Resource(R.id.ed_test2)
    @NotEmpty(message = "error_empty")
    private String test2 = "";

    @Resource(R.id.sp_test)
    @Email(message = "error_no_email")
    @NotEmpty(message = "error_empty")
    private String valueFromSpinner = "";

    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }

    public String getValueFromSpinner() {
        return valueFromSpinner;
    }

    public void setValueFromSpinner(String valueFromSpinner) {
        this.valueFromSpinner = valueFromSpinner;
    }
}
