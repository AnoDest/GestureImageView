package com.example.user.pinchzoomimage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by skamenkovych@codeminders.com on 9/15/2016.
 */
public class TouchImageViewSample extends ImageView {

    private static final String TAG = TouchImageViewSample.class.getSimpleName();
    private final float minDistance;

    private static final int INVALID_POINTER_ID = -1;

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    private final RectF imageRect = new RectF();
    private final RectF tempRect = new RectF();

    // Remember some things for zooming
    PointF start = new PointF();

    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    public TouchImageViewSample(Context context) {
        this(context, null, 0);
    }

    public TouchImageViewSample(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private float mMinScale = 1.f;

    // Existing code ...
    public TouchImageViewSample(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        minDistance = dpToPx(getContext(), 40);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getDrawable() == null) {
            return false;
        }
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        mScaleDetector.onTouchEvent(event);
        if (mScaleDetector.isInProgress()) {
            start.set(event.getX(), event.getY());
            return true;
        }
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                start.set(event.getX(), event.getY());
                tempRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
                matrix.mapRect(tempRect);
                float xShift = 0, yShift = 0;
                if (tempRect.left > 0) {
                    xShift = -tempRect.left;
                } else if (tempRect.right < getWidth()) {
                    xShift = getWidth() - tempRect.right;
                }
                if (tempRect.top > 0) {
                    yShift = -tempRect.top;
                } else if (tempRect.bottom < getHeight()) {
                    yShift = getHeight() - tempRect.bottom;
                }
                matrix.postTranslate(xShift, yShift);
                setImageMatrix(matrix);
                imageRect.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
                matrix.mapRect(imageRect);
                break;
            default:
                start.set(event.getX(), event.getY());
                break;
        }
        return true;
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        // Constrain to given size but keep aspect ratio
        setUpMatrix(drawable, getWidth(), getHeight());
        super.setImageDrawable(drawable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setUpMatrix(getDrawable(), w, h);
    }

    private void setUpMatrix(Drawable drawable, int w, int h) {
        if (drawable == null) {
            return;
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        mScaleFactor = Math.max(w / (float) width, h / (float) height);
        float centerViewX = w / 2;
        float centerViewY = h / 2;
        float centerImageX = width / 2;
        float centerImageY = height / 2;
        // TODO scale to pre-set rect or to crop
        matrix.reset();
        matrix.postTranslate(centerViewX - centerImageX, centerViewY - centerImageY);
        matrix.postScale(mScaleFactor, mScaleFactor, centerViewX, centerViewY);
        setImageMatrix(matrix);
        savedMatrix.set(matrix);
        imageRect.set(0, 0, width, height);
        matrix.mapRect(imageRect);
        mMinScale = mScaleFactor;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float width, height;
        private float[] focusPoint = new float[2];
        private float[] pivotPoint = new float[2];
        private float[] anchorPoint = new float[2];
        private float newScale;
        private float xShift, yShift;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float currentScale = detector.getScaleFactor();
            newScale = mScaleFactor * currentScale;

            focusPoint[0] = detector.getFocusX();
            focusPoint[1] = detector.getFocusY();

            if (newScale >= mMinScale) {
                mScaleFactor = newScale;
            } else {
                currentScale = mMinScale / mScaleFactor;
                mScaleFactor = mMinScale;
            }
            matrix.postTranslate(anchorPoint[0] - pivotPoint[0], anchorPoint[1] - pivotPoint[1]);
            pivotPoint[0] = focusPoint[0];
            pivotPoint[1] = focusPoint[1];
            matrix.postScale(currentScale, currentScale, anchorPoint[0], anchorPoint[1]);
            matrix.postTranslate(focusPoint[0] - anchorPoint[0], focusPoint[1] - anchorPoint[1]);
            tempRect.set(0, 0, width, height);
            matrix.mapRect(tempRect);
            if (tempRect.left > 0) {
                xShift = -tempRect.left;
            } else if (tempRect.right < getWidth()) {
                xShift = getWidth() - tempRect.right;
            }
            if (tempRect.top > 0) {
                yShift = -tempRect.top;
            } else if (tempRect.bottom < getHeight()) {
                yShift = getHeight() - tempRect.bottom;
            }
            matrix.postTranslate(xShift, yShift);
            pivotPoint[0] += xShift;
            pivotPoint[1] += yShift;
            setImageMatrix(matrix);
            imageRect.set(0, 0, width, height);
            matrix.mapRect(imageRect);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            anchorPoint[0] = detector.getFocusX();
            anchorPoint[1] = detector.getFocusY();
            pivotPoint[0] = anchorPoint[0];
            pivotPoint[1] = anchorPoint[1];
            width = getDrawable().getIntrinsicWidth();
            height = getDrawable().getIntrinsicHeight();
            return super.onScaleBegin(detector);
        }
    }

    public static int dpToPx(Context context, int dp){
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
