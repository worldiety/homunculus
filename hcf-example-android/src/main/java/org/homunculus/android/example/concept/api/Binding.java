package org.homunculus.android.example.concept.api;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public interface Binding<T,Scope> {
    T apply(Scope scope);
}
