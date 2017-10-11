package org.homunculusframework.factory.container;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Component<T> {
    @Nullable
    private final T value;
    private final List<Throwable> failures;

    public Component(T value, List<Throwable> failures) {
        this.value = value;
        this.failures = failures;
    }

    public Component(T value, Throwable failure) {
        this.value = value;
        this.failures = new ArrayList<>();
        this.failures.add(failure);
    }

    /**
     * Returns the component or null if creation failed. If not null, you should inspect
     * {@link #getFailures()} if you want to decide to use the result or not. Sometimes it may
     * be perfectly fine to ignore certain errors.
     */
    @Nullable
    public T get() {
        return value;
    }

    /**
     * Returns associated failures, usually occured while creating or injecting.
     */
    public List<Throwable> getFailures() {
        return failures;
    }
}
