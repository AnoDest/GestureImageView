package com.example.user.pinchzoomimage;

import android.app.Application;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        Matrix matrix = new Matrix();
        RectF rect = new RectF(0, 0, 4, 3);
        RectF init = new RectF(rect);
        float[] a = new float[2];
        float[] b = new float[2];
        setPoint(b, 2, 2);
        matrix.mapPoints(a, b);
        printPoint(b);
        printPoint(a);
        System.out.println("Scale by 2 in point b " + b[0] + " " + b[1]);
        matrix.postScale(2, 2, b[0], b[1]);
        matrix.mapPoints(a, b);
        printRect(matrix, init);
        printPoint(b);
        printPoint(a);
        System.out.println("Reset. Scale by 2 in point " + 3 + " " + 1);
        matrix.reset();
        matrix.postScale(2, 2, 3, 1);
        matrix.mapPoints(a, b);
        printRect(matrix, init);
        printPoint(b);
        printPoint(a);
        System.out.println("Scale by 2 in point " + 3 + " " + 1);
        matrix.postScale(2, 2, 3, 1);
        matrix.mapPoints(a, b);
        printRect(matrix, init);
        printPoint(b);
        printPoint(a);
        System.out.println("Reset. Scale by 2 in point " + 3 + " " + 1);
        matrix.reset();
        matrix.postScale(2, 2, 3, 1);
        matrix.mapPoints(a, b);
        printRect(matrix, init);
        printPoint(b);
        printPoint(a);
        System.out.println("Scale by 2 in point " + 3 + " " + 1 + " translate -1,-1");
        matrix.postScale(2, 2, 3, 1);
        matrix.postTranslate(-1, -1);
        matrix.mapPoints(a, b);
        printRect(matrix, init);
        printPoint(b);
        printPoint(a);
    }

    private static void printRect(Matrix matrix, RectF init) {
        RectF rect = new RectF();
        matrix.mapRect(rect, init);
        System.out.println(rect.toShortString());
    }

    private static void printPoint(float[] point) {
        System.out.println(String.format("[%f, %f]", point[0], point[1]));
    }

    private static void setPoint(float[] point, float x, float y) {
        point[0] = x;
        point[1] = y;
    }
}