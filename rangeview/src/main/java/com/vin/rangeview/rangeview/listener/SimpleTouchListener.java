package com.vin.rangeview.rangeview.listener;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public abstract class SimpleTouchListener implements View.OnTouchListener {

    /**
     * Flag determining whether the down touch has stayed with the bounds of the view.
     */
    private boolean touchStayedWithinViewBounds;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStayedWithinViewBounds = true;
                return onDownTouchAction();

            case MotionEvent.ACTION_UP:
                if (touchStayedWithinViewBounds) {
                    return onUpTouchAction();
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (touchStayedWithinViewBounds
                        && !isMotionEventInsideView(view, event)) {
                    boolean flag = onCancelTouchAction();
                    touchStayedWithinViewBounds = false;
                    return flag;
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                return onCancelTouchAction();

            default:
                return false;
        }
    }

    /**
     * Method which is called when the {@link View} is touched down.
     */
    public abstract boolean onDownTouchAction();

    /**
     * Method which is called when the down touch is released on the {@link View}.
     */
    public abstract boolean onUpTouchAction();

    /**
     * Method which is called when the down touch is canceled,
     * e.g. because the down touch moved outside the bounds of the {@link View}.
     */
    public abstract boolean onCancelTouchAction();

    /**
     * Determines whether the provided {@link MotionEvent} represents a touch event
     * that occurred within the bounds of the provided {@link View}.
     *
     * @param view  the {@link View} to which the {@link MotionEvent} has been dispatched.
     * @param event the {@link MotionEvent} of interest.
     * @return true iff the provided {@link MotionEvent} represents a touch event
     * that occurred within the bounds of the provided {@link View}.
     */
    private boolean isMotionEventInsideView(View view, MotionEvent event) {
        Rect viewRect = new Rect(
                view.getLeft(),
                view.getTop(),
                view.getRight(),
                view.getBottom()
        );

        return viewRect.contains(
                view.getLeft() + (int) event.getX(),
                view.getTop() + (int) event.getY()
        );
    }
}