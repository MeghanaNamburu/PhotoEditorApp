package com.example.helloworld;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ImageView tips;
    ImageView imageView;
    HorizontalScrollView toolsLayout;
    ConstraintLayout brightnessSeekBarLayout,contrastSeekBarLayout,saturationSeekBarLayout;
    TextView brightnessBtn,brightnessSeekBarOkView,contrastBtn,contrastSeekBarOkView,saturationBtn,saturationSeekBarOkView;
    SeekBar brightnessSeekerBar,contrastSeekerBar,saturationSeekerBar;
    BitmapDrawable ogBmp;
    Bitmap outputBitmap;
    float bprevprog, cprevprog , sprevprog ;
    String image_uri = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        imageView=findViewById(R.id.image);
        ogBmp=(BitmapDrawable) imageView.getDrawable();

        initializeViews();

        TextView saveImageButton = findViewById(R.id.saveImage);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                final DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==DialogInterface.BUTTON_POSITIVE){
                            final File outFile =createImageFile();
                            try(FileOutputStream out=new FileOutputStream(outFile)){
                                outputBitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                                imageUri =Uri.parse("file://"+outFile.getAbsolutePath());
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,imageUri));
                                Toast.makeText(MainActivity.this,"The image was saved",Toast.LENGTH_SHORT).show();
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                };
                builder.setMessage("Save current photo to gallery?").
                        setPositiveButton("yes",dialogClickListener).
                        setNegativeButton("no",dialogClickListener).show();
            }
        });

    }


    private void initializeViews() {
        toolsLayout=findViewById(R.id.toolsLayout);

        brightnessBtn=findViewById(R.id.brightnessBtn);
        brightnessSeekBarLayout=findViewById(R.id.brightnessSeekBarLayout);
        brightnessSeekBarOkView=findViewById(R.id.brightnessSeekBarOkView);
        brightnessSeekerBar=(SeekBar) findViewById(R.id.brightnessSeekBar);

        contrastBtn=findViewById(R.id.contrastBtn);
        contrastSeekBarLayout=findViewById(R.id.contrastSeekBarLayout);
        contrastSeekBarOkView=findViewById(R.id.contrastSeekBarOkView);
        contrastSeekerBar=(SeekBar) findViewById(R.id.contrastSeekBar);

        saturationBtn=findViewById(R.id.saturationBtn);
        saturationSeekBarLayout=findViewById(R.id.saturationSeekBarLayout);
        saturationSeekBarOkView=findViewById(R.id.saturationSeekBarOkView);
        saturationSeekerBar=(SeekBar) findViewById(R.id.saturationSeekBar);

        brightnessBtn.setOnClickListener(view -> {
            brightnessSeekBarLayout.setVisibility(view.VISIBLE);
            toolsLayout.setVisibility(view.GONE);
        });

        contrastBtn.setOnClickListener(view -> {
            contrastSeekBarLayout.setVisibility(view.VISIBLE);
            toolsLayout.setVisibility(view.GONE);
        });

        saturationBtn.setOnClickListener(view -> {
            saturationSeekBarLayout.setVisibility(view.VISIBLE);
            toolsLayout.setVisibility(view.GONE);
        });

        brightnessSeekBarOkView.setOnClickListener(view -> {
            brightnessSeekBarLayout.setVisibility(view.GONE);
            toolsLayout.setVisibility(view.VISIBLE);
            ogBmp=(BitmapDrawable) imageView.getDrawable();
        });

        contrastSeekBarOkView.setOnClickListener(view -> {
            contrastSeekBarLayout.setVisibility(view.GONE);
            toolsLayout.setVisibility(view.VISIBLE);
            ogBmp=(BitmapDrawable) imageView.getDrawable();
        });

        saturationSeekBarOkView.setOnClickListener(view -> {
            saturationSeekBarLayout.setVisibility(view.GONE);
            toolsLayout.setVisibility(view.VISIBLE);
            ogBmp=(BitmapDrawable) imageView.getDrawable();
        });

        bprevprog = (float) brightnessSeekerBar.getProgress();
        cprevprog = (float) contrastSeekerBar.getProgress();
        sprevprog = (float) saturationSeekerBar.getProgress();
        seekBarListeners();
    }

    private void seekBarListeners() {
        brightnessSeekerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                adjustBrightness(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        contrastSeekerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int pro, boolean b) {
                adjustContrast(pro);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        saturationSeekerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                setSat();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setSat() {
        ColorMatrix colorMatrix=new ColorMatrix();
        Paint cmpaint=new Paint();
        ogBmp=(BitmapDrawable) imageView.getDrawable();
        Bitmap bmp=ogBmp.getBitmap();
        outputBitmap=Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),false).copy(Bitmap.Config.ARGB_8888,true);
        Canvas cv=new Canvas(outputBitmap);

        float i=(float) saturationSeekerBar.getProgress();
        colorMatrix.setSaturation((float) i/256);

        sprevprog = i;
        ColorMatrixColorFilter cmfilter=new ColorMatrixColorFilter(colorMatrix);
        cmpaint.setColorFilter(cmfilter);
        cv.drawBitmap(bmp,0,0,cmpaint);
        imageView.setImageBitmap(outputBitmap);
    }

    private void adjustContrast(int va) {
        ogBmp=(BitmapDrawable) imageView.getDrawable();
        Bitmap bmp=ogBmp.getBitmap();
        String initialHex=Tool.hexScale()[va];
        String initialMul="0X"+initialHex+initialHex+initialHex;
        int mul=Integer.decode(initialMul);
        int add=0X000000;
        outputBitmap=Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),false).copy(Bitmap.Config.ARGB_8888,true);
        Paint paint=new Paint();
        ColorFilter colorFilter=new LightingColorFilter(mul,add);
        paint.setColorFilter(colorFilter);
        Canvas canvas=new Canvas(outputBitmap);
        canvas.drawBitmap(outputBitmap,0,0,paint);
        imageView.setImageBitmap(outputBitmap);
    }

    private void adjustBrightness(int va) {
        ogBmp=(BitmapDrawable) imageView.getDrawable();
        Bitmap bmp=ogBmp.getBitmap();
        final int mul=0XFFFFFF; //mul value must be constant at 255
        String initialHex=Tool.hexScale()[va];
        String initialAdd="0X"+initialHex+initialHex+initialHex;
        int add=Integer.decode(initialAdd);

        outputBitmap=Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),false).copy(Bitmap.Config.ARGB_8888,true);
        Paint paint=new Paint();
        ColorFilter colorFilter=new LightingColorFilter(mul,add);
        paint.setColorFilter(colorFilter);
        Canvas canvas=new Canvas(outputBitmap);
        canvas.drawBitmap(outputBitmap,0,0,paint);
        imageView.setImageBitmap(outputBitmap);
    }



    private void openDialog() {
        ExampleDialog exampleDialog=new ExampleDialog();
        exampleDialog.show(getSupportFragmentManager(),"example dialog");
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    public static final String[] PERMISSIONS={
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT=2;

    @SuppressLint("NewApi")
    private boolean notPermissions(){
        for(int i=0;i<PERMISSIONS_COUNT;i++){
            if(checkSelfPermission(PERMISSIONS[i])!=PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && notPermissions()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
            if(notPermissions()){
                ((ActivityManager) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                recreate();
            }
        }
    }
    public void onBackPressed(){
        if(editMode){
            findViewById(R.id.editScreen).setVisibility(View.GONE);
            findViewById(R.id.welcomescreen).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            editMode=false;
        }else{
            super.onBackPressed();
        }
    }
    private static final int REQUEST_PICK_IMAGE=12345;
    private void init(){
        if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.N){
            StrictMode.VmPolicy.Builder builder=new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        if (!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            findViewById(R.id.camBtn).setVisibility(View.GONE);
        }
        TextView selectImageButton = findViewById(R.id.addBtn);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                final Intent picIntent=new Intent(Intent.ACTION_PICK);
                picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                final Intent chooserIntent=Intent.createChooser(intent,"Select Image");
                startActivityForResult(chooserIntent,REQUEST_PICK_IMAGE);

            }
        });
        TextView takePhotoButton = findViewById(R.id.camBtn);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePictureIntent.resolveActivity(getPackageManager())!=null){
                    final File photoFile=createImageFile();
                    imageUri=Uri.fromFile(photoFile);
                    final SharedPreferences myPres=getSharedPreferences(appID,0);
                    myPres.edit().putString("path", photoFile.getAbsolutePath()).apply();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
                }else{
                    Toast.makeText(MainActivity.this,"Your camera app is not compatible",
                            Toast.LENGTH_SHORT).show();

                }

            }
        });

        TextView rotate = findViewById(R.id.rotate);
       rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Bitmap rotatedImage = rotateBitmap(outputBitmap);
                    saveBitmapToCache(rotatedImage);
                    outputBitmap= getBitmapFromCache();
                    imageView.setImageBitmap(outputBitmap);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public Bitmap rotateBitmap(Bitmap bitmap){
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postScale((float)1, (float)1);
        matrix.postRotate(90);

        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return bitmap2;
    }

    public void saveBitmapToCache(Bitmap bitmap) throws IOException {
        String filename = "final_image.jpg";
        File cacheFile = new File(getApplicationContext().getCacheDir(), filename);
        OutputStream out = new FileOutputStream(cacheFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, (int)100, out);
        out.flush();
        out.close();
    }

    public Bitmap getBitmapFromCache(){
        File cacheFile = new File(getApplicationContext().getCacheDir(), "final_image.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
        return myBitmap;
    }

    private static final int REQUEST_IMAGE_CAPTURE=1012;
    private static final String appID="photoEditor";
    private Uri imageUri;
    private File createImageFile(){
        final String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName="/JPEG_"+timeStamp+".jpg";
        final File storageDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir+imageFileName);
    }
    private boolean editMode=false;
    private int width=0;
    private int height=0;
    private static final int MAX_PIXEL_COUNT=2048;
    private int [] pixels;
    private int pixelCount=0;
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode !=RESULT_OK){
            return;
        }
        if(requestCode==REQUEST_IMAGE_CAPTURE){
            if(imageUri==null){
                final SharedPreferences p=getSharedPreferences(appID,0);
                final String path=p.getString("path","");
                if(path.length()<1){
                    recreate();
                    return;
                }
                imageUri=Uri.parse("file://"+path);
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,imageUri));
        }else if(data==null){
            recreate();
            return;
        }else if(requestCode==REQUEST_PICK_IMAGE){
            imageUri=data.getData();
        }
        final ProgressDialog dialog=ProgressDialog.show(MainActivity.this,"Loading","Please Wait",true);
        editMode=true;

        findViewById(R.id.welcomescreen).setVisibility(View.GONE);
        findViewById(R.id.editScreen).setVisibility(View.VISIBLE);
        new Thread(){
            public void run() {
                //bitmap = null;
                final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inBitmap =outputBitmap;
                bmpOptions.inJustDecodeBounds = true;
                try (InputStream input = getContentResolver().openInputStream(imageUri)) {
                    outputBitmap = BitmapFactory.decodeStream(input, null, bmpOptions);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmpOptions.inJustDecodeBounds = false;
                width = bmpOptions.outWidth;
                height = bmpOptions.outHeight;
                int resizeScale = 1;
                if (width > MAX_PIXEL_COUNT) {
                    resizeScale=width/MAX_PIXEL_COUNT;
                } else if (height > MAX_PIXEL_COUNT) {
                    resizeScale=height/MAX_PIXEL_COUNT;
                }
                if(width/resizeScale>MAX_PIXEL_COUNT || height/resizeScale>MAX_PIXEL_COUNT){
                    resizeScale++;
                }
                bmpOptions.inSampleSize=resizeScale;
                InputStream input=null;
                try{
                    input=getContentResolver().openInputStream(imageUri);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    recreate();
                    return;
                }
                outputBitmap=BitmapFactory.decodeStream(input,null,bmpOptions);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(outputBitmap);
                        dialog.cancel();
                    }
                });
                width =outputBitmap.getWidth();
                height =outputBitmap.getHeight();
                outputBitmap =outputBitmap.copy(Bitmap.Config.ARGB_8888,true);
                pixelCount=width*height;
                pixels=new int[pixelCount];
                outputBitmap.getPixels(pixels,0,width,0,0,width,height);
            }
        }.start();

        TextView backButton = findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.editScreen).setVisibility(View.GONE);
                findViewById(R.id.welcomescreen).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
                editMode=false;
            }
        });

    }
}