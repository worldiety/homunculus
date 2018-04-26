package org.homunculusframework.factory.container;

import org.homunculusframework.factory.scope.ContextScope;
import org.homunculusframework.navigation.Navigation;

import java.util.List;

/**
 * Created by Torben Schinke on 26.04.18.
 */

public abstract class HistoryProcessorBinding<Out extends ContextScope<?>, In extends ContextScope<?>> extends ObjectBinding<Out, In> {

    private final ObjectBinding<Out, In> delegate;

    /**
     * intentionally private to avoid unintentional leaks and to be somewhat compatible with interprocess frontend/backend.
     * See {@link #pop(ObjectBinding, int)}
     */
    @Deprecated
    public HistoryProcessorBinding(ObjectBinding<Out, In> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Out create(In scope) throws Exception {
        Navigation navigation = scope.resolve(Navigation.class);
        if (navigation != null) {
            onApply(navigation.getStack());
        }
        return delegate.create(scope);
    }

    public ObjectBinding<Out, In> getDelegate() {
        return delegate;
    }

    public abstract void onApply(List<Binding<?, ?>> stack);


    /**
     * Wraps a given object binding and pops the given amount of entries from the navigation stack at the time of execution. The {@link org.homunculusframework.navigation.DefaultNavigation}
     * will unwrap and pushes on top the stack, if it is not returned from a controller method.
     */
    public static <Out extends ContextScope<?>, In extends ContextScope<?>> HistoryProcessorBinding<Out, In> pop(ObjectBinding<Out, In> binding, int entries) {
        return new HistoryProcessorBinding<Out, In>(binding) {
            @Override
            public void onApply(List<Binding<?, ?>> stack) {
                for (int i = 0; i < entries; i++) {
                    if (stack.size() > 0) {
                        stack.remove(stack.size() - 1);
                    }
                }
            }
        };
    }
}
