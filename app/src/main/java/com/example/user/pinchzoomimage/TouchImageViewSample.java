package com.example.user.pinchzoomimage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
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

    private float mPosX = 0f;
    private float mPosY = 0f;

    private float mLastTouchX;
    private float mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;

    // These matrices will be used to move and zoom image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    private static final Matrix IDENTITY = new Matrix();
    private final RectF tempRect = new RectF();
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    String savedItemClicked;

    float pivotPointX = 0f;
    float pivotPointY = 0f;

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

    // Existing code ...
    public TouchImageViewSample(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        minDistance = dpToPx(getContext(), 40);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleDetector.onTouchEvent(event);
        //if ((event.getAction() & MotionEvent.ACTION_MASK) != MotionEvent.ACTION_MOVE) {
        //    dumpEvent(event);
        //}
        //
        //// Handle touch events here...
        //switch (event.getAction() & MotionEvent.ACTION_MASK) {
        //    case MotionEvent.ACTION_DOWN:
        //        savedMatrix.set(matrix);
        //        start.set(event.getX(), event.getY());
        //        Log.d(TAG, "mode=DRAG");
        //        mode = DRAG;
        //        break;
        //    case MotionEvent.ACTION_POINTER_DOWN:
        //        oldDist = spacing(event);
        //        Log.d(TAG, "oldDist=" + oldDist);
        //        if (oldDist > 300f) {
        //            midPoint(mid, event);
        //            mode = ZOOM;
        //            Log.d(TAG, "mode=ZOOM");
        //        } else {
        //            mode = DRAG;
        //        }
        //        break;
        //    case MotionEvent.ACTION_UP:
        //    case MotionEvent.ACTION_POINTER_UP:
        //        mode = NONE;
        //        Log.d(TAG, "mode=NONE");
        //        break;
        //    case MotionEvent.ACTION_MOVE:
        //        if (mode == DRAG) {
        //            // ...
        //            matrix.set(savedMatrix);
        //            matrix.postTranslate(event.getX() - start.x, event.getY()
        //                    - start.y);
        //        } else if (mode == ZOOM) {
        //            float newDist = spacing(event);
        //            Log.d(TAG, "newDist=" + newDist + " " + minDistance);
        //            if (newDist > minDistance) {
        //                matrix.set(savedMatrix);
        //                float scale = newDist / oldDist;
        //                matrix.postScale(scale, scale, mid.x, mid.y);
        //                matrix.postTranslate(event.getX() - start.x, event.getY()
        //                        - start.y);
        //            } else {
        //                matrix.set(savedMatrix);
        //                matrix.postTranslate(event.getX() - start.x, event.getY()
        //                        - start.y);
        //            }
        //        }
        //        break;
        //}
        //
        //setImageMatrix(matrix);
        //return true;
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

    /** Determine the space between the first two fingers */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable
     * )
     */
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
        mLastTouchX = mPosX = 0;
        mLastTouchY = mPosY = 0;

        mScaleFactor = Math.max(w / (float) width, h / (float) height);
        pivotPointX = width * mScaleFactor / 2;
        pivotPointY = height * mScaleFactor / 2;

        // TODO scale to pre-set rect or to crop
        matrix.set(IDENTITY);
        matrix.postTranslate(pivotPointX - width / 2, height / 2 - pivotPointY);
        matrix.postScale(mScaleFactor, mScaleFactor, pivotPointX, pivotPointY);
        setImageMatrix(matrix);
        tempRect.set(0, 0, width, height);
        matrix.mapRect(tempRect);
        Log.d(TAG, "Rect: " + tempRect.toShortString());
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float x, y;
        private float width, height;
        private float initialScale;
        private float[] focusPoint = new float[2];
        private float[] pivotPoint = new float[2];
        private float xShift, yShift;
        boolean isFirst;
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            focusPoint[0] = detector.getFocusX();
            focusPoint[1] = detector.getFocusY();

            xShift = (focusPoint[0] - xShift) * detector.getScaleFactor();
            yShift = (focusPoint[1] - yShift) * detector.getScaleFactor();

            matrix.mapPoints(pivotPoint, focusPoint);
            if (isFirst) {
                Log.d(TAG, "scale " + mScaleFactor + " det " + detector.getScaleFactor());
                Log.d(TAG, "fX " + focusPoint[0] + " fY " + focusPoint[1]);
                Log.d(TAG, "mapX " + pivotPoint[0] + " mapY " + pivotPoint[1]);
                isFirst = false;
            }
            //mScaleFactor = Math.max(0.05f, mScaleFactor);
            //matrix.reset();
            //pivotPointX = pivotPoint[0] - pivotPointX;
            //pivotPointY = pivotPoint[1];
            //matrix.postTranslate(pivotPointX - width / 2, height / 2 - pivotPointY);
            //matrix.postScale(mScaleFactor, mScaleFactor, pivotPointX, pivotPointY);
            //setImageMatrix(matrix);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            x = detector.getFocusX();
            y = detector.getFocusY();
            initialScale = mScaleFactor;
            savedMatrix.set(matrix);
            isFirst = true;
            Log.d(TAG, "det " + detector.getScaleFactor());
            Log.d(TAG, "x " + x + " y " + y);
            return super.onScaleBegin(detector);
        }
    }

    public static int dpToPx(Context context, int dp){
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}