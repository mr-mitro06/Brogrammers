package com.example.motionbrush;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private float brushX, brushY;
    private Paint paint;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView = new SurfaceView(this);
        holder = surfaceView.getHolder();
        setContentView(surfaceView);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10f);

        brushX = 500; // start center-ish
        brushY = 800;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float ax = event.values[0]; // left-right
        float ay = event.values[1]; // up-down

        brushX -= ax * 5; // multiply for sensitivity
        brushY += ay * 5;

        drawBrush();
    }

    private void drawBrush() {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.WHITE); // clear
            canvas.drawCircle(brushX, brushY, 20, paint);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
}
