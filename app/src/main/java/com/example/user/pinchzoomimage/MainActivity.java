package com.example.user.pinchzoomimage;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TouchImageViewSample view;
    static final int REQUEST_TAKE_PHOTO = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (TouchImageViewSample) findViewById(R.id.imageView);
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            BitmapDrawable drawable = new BitmapDrawable(getResources(), mCurrentPhotoPath.replaceFirst("file:", ""));
            float w = drawable.getBitmap().getWidth();
            float h = drawable.getBitmap().getHeight();
            FaceDetector detector = new FaceDetector((int) w, (int) h, 1);
            FaceDetector.Face[] faces = new FaceDetector.Face[1];
            int faceNumber = detector.findFaces(drawable.getBitmap().copy(Bitmap.Config.RGB_565, false), faces);
            if (faceNumber == 1 && faces[0].confidence() >= 0.3f) {
                float distance = faces[0].eyesDistance();
                PointF midPoint = new PointF();
                faces[0].getMidPoint(midPoint);
                Log.d(TAG, midPoint.toString() + " " + distance + " " + w + " " + h + " " + drawable.getIntrinsicWidth() + " " + drawable.getIntrinsicHeight());
                float scale = 1.7f;
                RectF imageRect = new RectF(midPoint.x - distance * 3 / scale,
                        midPoint.y - distance * 3 / scale,
                        midPoint.x + distance * 3 / scale,
                        midPoint.y + distance * 5 / scale);
                Log.d(TAG, "Rect: " + imageRect.toShortString());
                if (imageRect.right < w && imageRect.left >= 0 && imageRect.bottom < h && imageRect.top >= 0) {
                    Matrix matrix = new Matrix();
                    matrix.setScale(drawable.getIntrinsicWidth() /w, drawable.getIntrinsicHeight() / h);
                    matrix.mapRect(imageRect);
                    view.setImageDrawable(drawable, new RectF(0.2f, 0.1f, 0.8f, 0.9f), imageRect);
                    return;
                }
            }
            view.setImageDrawable(drawable);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
