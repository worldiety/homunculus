package org.homunculusframework.factory.flavor.hcf;

import java.lang.annotation.*;

/**
 * You can specify multiple methods with an exact execution order. The lower the order value, the earlier it get's executed.
 * If the order is the same for multiple methods, the execution order is undefined.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Priority {

    int value();

}
