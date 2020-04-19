package com.example.printscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private View shotView;
    private Button btn;
    private Button btn2;
    private Button btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn_save);
        btn2 = findViewById(R.id.btn_save2);
        btn3 = findViewById(R.id.btn_save3);
        shotView = findViewById(R.id.shotView);

        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);

    }


    // 獲取螢幕(擷取範圍)方法1， API28後已不建議使用
    public void screenshot1() {
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();

        SaveShopImage(bmp);
    }


    //獲取螢幕(擷取範圍)方法2， 官方推薦使用PixelCopy
    public void screenshot2() {

        //这里将LinearLayout布局转换成Bitmap给ImageView显示，也可使用getWindow().getDecorView()取得所有布局
        //准备一个bitmap对象，用来将copy出来的区域绘制到此对象中
        final Bitmap bmp = Bitmap.createBitmap(shotView.getWidth(), shotView.getHeight(), Bitmap.Config.ARGB_8888, true);//最後一項是色彩模式

        //获取layout的位置，取得的座標位置會放置到location裡面
        final int[] location = new int[2];
        shotView.getLocationInWindow(location);

        //请求转换
        PixelCopy.request(
                getWindow(),
                new Rect(location[0], location[1], location[0] + shotView.getWidth(), location[1] + shotView.getHeight()),
                bmp,
                new PixelCopy.OnPixelCopyFinishedListener() {
                    @Override
                    public void onPixelCopyFinished(int copyResult) {
                        //如果成功
                        if (copyResult == PixelCopy.SUCCESS) {
                            if (bmp != null) {
                                SaveShopImage(bmp);
                            } else {
                                Toast.makeText(MainActivity.this, "图片取得失败", Toast.LENGTH_LONG).show();
                                Log.d("MainActivity", "onPixelCopyFinished: bitmap = null");
                            }
                        }
                    }
                },
                new Handler(Looper.getMainLooper())
        );
    }


    //存取挡案
    private void SaveShopImage(Bitmap bmp) {
        //檔名設置
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        NumberFormat nf = new DecimalFormat("000");
        int count = 1;
        String finename = "Screen_" + sdf.format(new Date());
        // 圖片檔案路徑
        File file = new File(getExternalFilesDir("SCREEN"), finename + "_001.png");
        while ((file.exists())) {
            file = new File(getExternalFilesDir("SCREEN"), finename + "_" + nf.format(count) + ".png");
            count++;
        }
        try {
            FileOutputStream os = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            Log.d("MainActivity", "screenshot: 创建:" + file);
        } catch (Exception e) {
            Log.e("MainActivity", "screenshot: " + e.toString());
        }
    }


    public void screenshot3(){

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;     // 螢幕寬度（畫素）
        int screenHeight = metric.heightPixels;   // 螢幕高度（畫素）
        // rect是描述一个截取部分图的方块，我这里是截取全部，所以是(0, 0, 0, 0)
        // screenWidth 屏幕宽度
        // screenHeight 屏幕高度
        // rotation 屏幕旋转角度，整数0|90|180|270
        Rect rect = new Rect(0, 0, 0, 0);
        int rotation = 0;
//        int screenWidth = 1080;
//        int screenHeight = 1920;

        try {
            Bitmap bitmap = (Bitmap) Class.forName("android.view.SurfaceControl").getDeclaredMethod("screenshot",  new Class[]{Rect.class, Integer.TYPE, Integer.TYPE, Integer.TYPE })
                    .invoke(null, new Object[]{rect, screenWidth, screenHeight, rotation});
            if (bitmap == null){
                Log.d(TAG, "screenshot3: null");
            }else {
                SaveShopImage(bitmap);
            }
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException: "+e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException: "+e.toString());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException: "+e.toString());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException: "+e.toString());
        }

    }


    @Override
    public void onClick(View v) {
        if (v == btn){
            screenshot1();
        }else if(v == btn2){
            screenshot2();
        }else if(v == btn3){
            screenshot3();
        }

    }
}
