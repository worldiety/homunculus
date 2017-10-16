package org.homunculusframework.factory.flavor.hcf;

import org.homunculusframework.factory.container.Container;

import java.lang.annotation.*;

/**
 * Denotes a handler name from a scope for execute a class' constructor and destructor and injections.
 * This does not influence the execution of {@link javax.annotation.PostConstruct} or {@link javax.annotation.PreDestroy},
 * but applies when specifically set on such methods.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Execute {

    /**
     * The name of the {@link org.homunculusframework.factory.container.Handler} used to create or inflate a Component (e.g. a {@link Controller} or a
     * {@link Widget}). The default for a Controller is inline (== the calling thread) and for a Widget
     * the {@link Container#NAME_MAIN_HANDLER}. Here it may make sense
     * to use the {@link Container#NAME_INFLATER_HANDLER}, e.g. on Android.
     */
    String value();
}
