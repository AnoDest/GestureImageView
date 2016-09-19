package com.example.user.pinchzoomimage;

import android.graphics.Matrix;
import android.graphics.RectF;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        Matrix matrix = new Matrix();
        RectF rect = new RectF(0, 0, 4, 3);
        RectF init = new RectF(rect);
        float[] a = new float[2];
        float[] b = new float[2];
        setPoint(b, 2, 2);
        matrix.mapPoints(a, b);
        printPoint(b);
        printPoint(a);
        matrix.postScale(2, 2, b[0], b[1]);
        matrix.mapPoints(a, b);
        printPoint(b);
        printPoint(a);
        printRect(matrix, init);
        matrix.reset();
        matrix.postScale(2, 2, a[0], a[1]);
        matrix.mapPoints(a, b);
        printPoint(b);
        printPoint(a);
        printRect(matrix, init);
    }

    private static void printRect(Matrix matrix, RectF init) {
        RectF rect = new RectF();
        matrix.mapRect(rect, init);
        System.out.println(rect.toShortString());
    }

    private static void printPoint(float[] point) {
        System.out.println(String.format("[%d, %d]", point[0], point[1]));
    }

    private static void setPoint(float[] point, float x, float y) {
        point[0] = x;
        point[1] = y;
    }
}