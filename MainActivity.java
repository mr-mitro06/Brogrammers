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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private float brushX, brushY;
    private Paint paint;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private List<float[]> pathPoints;
    private float velocityX = 0, velocityY = 0;
    private int brushColor = Color.RED;

    private long lastTapTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView = new SurfaceView(this);
        holder = surfaceView.getHolder();
        setContentView(surfaceView);

        paint = new Paint();
        paint.setColor(brushColor);
        paint.setStrokeWidth(15f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        brushX = 500;
        brushY = 800;

        pathPoints = new ArrayList<>();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);

        surfaceView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();
                if (now - lastTapTime < 300) { // double tap
                    pathPoints.clear();
                } else {
                    // Single tap → change color
                    brushColor = getRandomColor();
                    paint.setColor(brushColor);
                }
                lastTapTime = now;
            }
            return true;
        });
    }

    private int getRandomColor() {
        return Color.rgb((int)(Math.random()*255),
                         (int)(Math.random()*255),
                         (int)(Math.random()*255));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            velocityX += -event.values[0] * 0.5f; // tilt left/right
            velocityY += event.values[1] * 0.5f;  // tilt up/down
        }

        brushX += velocityX;
        brushY += velocityY;

        // Keep within screen bounds
        brushX = Math.max(0, Math.min(surfaceView.getWidth(), brushX));
        brushY = Math.max(0, Math.min(surfaceView.getHeight(), brushY));

        // Add new point
        pathPoints.add(new float[]{brushX, brushY});

        drawCanvas();
    }

    private void drawCanvas() {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);

            // Draw trail
            for (float[] point : pathPoints) {
                canvas.drawCircle(point[0], point[1], 8, paint);
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME);
    }
}
