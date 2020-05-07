package com.example.printscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

//擷取APP畫面的方法練習
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private View shotView;
    private Button btn;
    private Button btn2;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn_save);
        btn2 = findViewById(R.id.btn_save2);
        shotView = findViewById(R.id.shotView);

        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);

    }

    // 獲取螢幕(擷取範圍)方法1， API28後已不建議使用
    public void screenshot1() {
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();
        SaveShopImage(bmp);
        Toast.makeText(MainActivity.this, "已截圖", Toast.LENGTH_SHORT).show();
    }


    //獲取螢幕(擷取範圍)方法2， 官方推薦使用PixelCopy
    public void screenshot2() {

        //這裡設置了一個View轉換成Bitmap給ImageView顯示，也可使用getWindow().getDecorView()取得所有布局
        //準備一個bitmap對象，用來將copy出來的區域繪製到此對象中
        bmp = Bitmap.createBitmap(shotView.getWidth(), shotView.getHeight(), Bitmap.Config.ARGB_8888, true);//最後一項是色彩模式
        //獲取layout的位置，取得的座標位置會放置到location裡面
        final int[] location = new int[2];
        shotView.getLocationInWindow(location);

        //請求轉換
        PixelCopy.request(
                getWindow(),
                new Rect(location[0], location[1], location[0] + shotView.getWidth(), location[1] + shotView.getHeight()),
                bmp,
                new PixelCopy.OnPixelCopyFinishedListener() {
                    @Override
                    public void onPixelCopyFinished(int copyResult) {
                        //如果成功
                        if (copyResult == PixelCopy.SUCCESS) {
                            SaveShopImage(bmp);
                            Toast.makeText(MainActivity.this, "已截圖", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Handler(Looper.getMainLooper())
        );
    }


    //存取擋案
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
            Log.d("MainActivity", "screenshot: 創建:" + file);
        } catch (Exception e) {
            Log.e("MainActivity", "screenshot: " + e.toString());
        }
    }


    @Override
    public void onClick(View v) {
        if (v == btn) {
            screenshot1();
        } else if (v == btn2) {
            screenshot2();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bmp != null){
            bmp.recycle();
        }
    }
}
