package com.example.omar.photofi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends ActionBarActivity {

    private static final int SELECTED_PHOTO = 1;
    ImageView mimageView;
    Bitmap prev;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mimageView = (ImageView) findViewById(R.id.imageView);
        mimageView.setAdjustViewBounds(true);

        mAttacher = new PhotoViewAttacher(mimageView);

    }

    public void DirtyFilter() {
        Drawable[] layers = new Drawable[2];
        layers[0] = mimageView.getDrawable();
        layers[1] = getResources().getDrawable(R.drawable.dirty);
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        mimageView.setImageDrawable(layerDrawable);
        mAttacher.update();

    }

    public void toGrayscale()
    {
        Bitmap bmpOriginal = ((BitmapDrawable) mimageView.getDrawable()).getBitmap();;
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);

        mimageView.setImageBitmap(bmpGrayscale);
        mAttacher.update();
    }

    public void invertBitmap() {
        Bitmap source = ((BitmapDrawable) mimageView.getDrawable()).getBitmap();

        Bitmap finalImage = Bitmap.createBitmap(source.getWidth(),source.getHeight(),source.getConfig());
        int A,R,G,B;
        int pixelColor;
        int height = source.getHeight();
        int width = source.getWidth();

        for (int i=0;i<height ;i++) {
            for (int j=0;j<width;j++) {
                pixelColor = source.getPixel(j,i);
                A = Color.alpha(pixelColor);
                R = 255 - Color.red(pixelColor);
                G = 255 - Color.green(pixelColor);
                B = 255 - Color.blue(pixelColor);
                finalImage.setPixel(j,i,Color.argb(A,R,G,B));
            }
        }

        mimageView.setImageBitmap(finalImage);
        mAttacher.update();
    }

    public void tapToSave()
    {

        Bitmap bitmap =((BitmapDrawable)mimageView.getDrawable()).getBitmap();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/"+currentDateAndTime+".jpg");
        try
        {
            file.createNewFile();
            FileOutputStream oStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, oStream);
            oStream.close();
            addImageToGallery(file.getAbsolutePath(),getApplicationContext());
            Toast toast = Toast.makeText(getApplicationContext(),"Photo Saved !",Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(),"Failure !",Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void savePrev() {
        if(mimageView.getDrawable()!=null) {
            prev = ((BitmapDrawable) mimageView.getDrawable()).getBitmap();
        }
    }

    public void undo() {
        if(prev!=null) {
            mimageView.setImageBitmap(prev);
            mAttacher.update();
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



    public void rotateR(View v) {
        if(mimageView.getDrawable()!=null) {
            savePrev();
            mimageView.setImageBitmap(RotateBitmap(((BitmapDrawable) mimageView.getDrawable()).getBitmap(), 90));
            mAttacher.update();
        }
    }

    public void rotateL(View v) {
        if(mimageView.getDrawable()!=null) {
            savePrev();
            mimageView.setImageBitmap(RotateBitmap(((BitmapDrawable) mimageView.getDrawable()).getBitmap(), -90));
            mAttacher.update();
        }
    }


    public void btnClick(View v) {
        savePrev();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,SELECTED_PHOTO);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String filePath,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == SELECTED_PHOTO && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            mimageView.setImageBitmap(decodeSampledBitmapFromResource(picturePath, 300, 300));

            mAttacher.update();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(mimageView.getDrawable()!=null) {
//            savePrev();
            if (id == R.id.action_settings) {
                return true;
            } else if(id == R.id.save_photo) {
                tapToSave();
                return true;
            } else if(id == R.id.invert_photo) {
                savePrev();
                invertBitmap();
                return true;
            } else if(id == R.id.dirty_layer) {
                savePrev();
                DirtyFilter();
                return true;
            } else if(id == R.id.gray_scale) {
                savePrev();
                toGrayscale();
                return true;
            } else if(id == R.id.undo) {
                undo();
                return true;
            }
        }




        return super.onOptionsItemSelected(item);
    }
}
