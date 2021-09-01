package org.homunculus.android.component;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.transition.TransitionManager;

import org.homunculusframework.navigation.Navigation;

/**
 * Component, which main purpose is to set a {@link View} as content view of an {@link Activity} using a {@link Transition}.
 * It is using the {@link Navigation} to determine, if it should use the forward or backward animation. The class also offers
 * a method to add a {@link View} to a {@link ViewGroup} using the same transitions, for the case, you don't want to animate a whole {@link View}.
 * <p>
 * Created by aerlemann on 06.03.18.
 */

public class ViewTransitionAnimator {
    private Navigation mNavigation;
    private Activity mActivity;

    private Transition mForwardTransition = new AutoTransition();
    private Transition mBackwardTransition = new AutoTransition();

    private ViewTransitionAnimator() {

    }

    public ViewTransitionAnimator(Navigation navigation, Activity activity) {
        this.mNavigation = navigation;
        this.mActivity = activity;
    }

    /**
     * Sets a {@link View} as content view of an {@link Activity} using a {@link Transition}, based on the current mNavigation direction
     * (forward or backward). The transitions may be set with {@link #setForwardTransition(Transition)} or {@link #setBackwardTransition(Transition)}.
     *
     * @param view the {@link View} to be set as content view
     */
    public void setActivityContentView(View view) {
        // Start recording changes to the view hierarchy
        TransitionManager.beginDelayedTransition((ViewGroup) mActivity.getWindow().getDecorView(), getTransitionToBeUsed());
        mActivity.setContentView(view);
    }

    private Transition getTransitionToBeUsed() {
        Transition transitionToBeUsed;
        if (mNavigation.wasGoingForward()) {
            transitionToBeUsed = mForwardTransition;
        } else {
            transitionToBeUsed = mBackwardTransition;
        }
        return transitionToBeUsed;
    }

    /**
     * Adds a {@link View} to a {@link ViewGroup} using a {@link Transition}. The transitions may be set with {@link #setForwardTransition(Transition)}
     * or {@link #setBackwardTransition(Transition)}.
     *
     * @param viewGroupToBeAddedTo the {@link ViewGroup} you want to add a view to
     * @param viewToBeAdded        the {@link View} you want to be added
     * @param layoutParams         the {@link LayoutParams} you would normally use to add the {@link View}
     */
    public void addViewToViewGroup(ViewGroup viewGroupToBeAddedTo, View viewToBeAdded, LayoutParams layoutParams) {
        // Start recording changes to the view hierarchy
        TransitionManager.beginDelayedTransition(viewGroupToBeAddedTo, getTransitionToBeUsed());
        viewGroupToBeAddedTo.addView(viewToBeAdded, layoutParams);
    }

    /**
     * Sets the animation for going forward in the {@link Navigation}.
     *
     * @param transitionId the id if the {@link Transition} to be inflated from xml
     */
    public void setForwardTransition(int transitionId) {
        mForwardTransition = TransitionInflater.from(mActivity).inflateTransition(transitionId);
    }

    /**
     * Sets the animation for going backward in the {@link Navigation}.
     *
     * @param transitionId the id if the {@link Transition} to be inflated from xml
     */
    public void setBackwardTransition(int transitionId) {
        mBackwardTransition = TransitionInflater.from(mActivity).inflateTransition(transitionId);
    }

    /**
     * Sets the animation for going forward in the {@link Navigation}.
     *
     * @param transition the {@link Transition} to be set
     */
    public void setForwardTransition(Transition transition) {
        mForwardTransition = transition;
    }

    /**
     * Sets the animation for going backward in the {@link Navigation}.
     *
     * @param transition the {@link Transition} to be set
     */
    public void setBackwardTransition(Transition transition) {
        mBackwardTransition = transition;
    }
}
