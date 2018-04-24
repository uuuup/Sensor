package com.amazing.magnetsensor;

import java.io.File;
import java.io.FileOutputStream;


import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazing.magnetsensor.MagnetSensor;
import com.amazing.magnetsensor.MagnetSensorEvent;
import com.amazing.magnetsensor.MagnetSensorListener;
import com.amazing.main.R;

public class MainActivity extends AppCompatActivity
{
    MagnetSensor magnetSensor = null;

    private Button btn_save, btn_resume,btn_initial;
    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        magnetSensor = new MagnetSensor(this);
        magnetSensor.register();

        // 初始化一个画笔，笔触宽度为5，颜色为红色
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15);

        iv_canvas = (ImageView) findViewById(R.id.iv_canvas);
        btn_initial = (Button) findViewById(R.id.btn_initial);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_resume = (Button) findViewById(R.id.btn_resume);

        btn_initial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                magnetSensor.initial();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBitmap();
            }
        });
        btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeCanvas();
            }
        });

        magnetSensor.setMagnetSensorListener(new MagnetSensorListener() {
            float startX;
            float startY;

            @Override
            public void onMagnetSensorEvent(MagnetSensorEvent event) {
                if (baseBitmap == null) {
                    baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                            iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.WHITE);
                }
                switch (event.type) {
                    //初始化
                    case SENSOR_MESSAGE_CLICK:
                        Toast.makeText(MainActivity.this, "点击事件" + event.values[0] + "\n" + event.values[1] + "\n" + event.values[2], Toast.LENGTH_SHORT).show();


                        // 记录开始触摸的点的坐标
                        startX = event.values[0];
                        startY = event.values[1];
                        break;
                    // 移动动作
                    case SENSOR_MESSAGE_MOVE:
                        Toast.makeText(MainActivity.this, "移动事件" + event.values[0] + "\n" + event.values[1] + "\n" + event.values[2], Toast.LENGTH_SHORT).show();

                        // 记录移动位置的点的坐标
                        float stopX = event.values[0];
                        float stopY = event.values[1];

                        //根据两点坐标，绘制连线
                        canvas.drawLine(startX, startY, stopX, stopY, paint);

                        // 更新开始点的位置
                        startX = event.values[0];
                        startY = event.values[1];

                        // 把图片展示到ImageView中
                        iv_canvas.setImageBitmap(baseBitmap);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        });
    }

    /**
     * 保存图片到SD卡上
     */
    protected void saveBitmap() {
        try {
            // 保存图片到SD卡上
            File file = new File(Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(file);
            baseBitmap.compress(CompressFormat.PNG, 100, stream);
            Toast.makeText(MainActivity.this, "保存图片成功",  Toast.LENGTH_SHORT).show();

            // Android设备Gallery应用只会在启动的时候扫描系统文件夹
            // 这里模拟一个媒体装载的广播，用于使保存的图片可以在Gallery中查看
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
            intent.setData(Uri.fromFile(Environment
                    .getExternalStorageDirectory()));
            sendBroadcast(intent);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "保存图片失败",  Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * 清除画板
     */

    protected void resumeCanvas() {
        // 手动清除画板的绘图，重新创建一个画板
        if (baseBitmap != null) {
            baseBitmap = Bitmap.createBitmap(iv_canvas.getWidth(),
                    iv_canvas.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
            canvas.drawColor(Color.WHITE);
            iv_canvas.setImageBitmap(baseBitmap);
            Toast.makeText(MainActivity.this, "清除画板成功，可以重新开始绘图",  Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true;
    }
}

