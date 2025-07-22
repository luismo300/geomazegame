package com.example.geomazegame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    protected SensorManager sensorManager;
    protected Sensor gravitySensor, rotationSensor;

    private TextView textNivel;

    protected ImageView selector;
    protected ImageView option1, option2, option3;
    protected TextView textQuestion;

    protected float selectorX, selectorY;
    protected float selectorSpeed = 10f;

    protected Handler handler = new Handler();
    protected Runnable checkSelectionRunnable;

    protected int screenWidth, screenHeight;

    protected int correctDrawableId;  // Variable configurable por nivel

    private boolean canValidate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selector = findViewById(R.id.selector);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        textQuestion = findViewById(R.id.textQuestion);
        textNivel = findViewById(R.id.textNivel);

        int numeroNivel = getIntent().getIntExtra("level", 1);
        textNivel.setText("Nivel " + numeroNivel);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        selectorX = screenWidth / 2f - selector.getWidth() / 2f;
        selectorY = 200;
        selector.setX(selectorX);
        selector.setY(selectorY);

        loadLevel(numeroNivel);

        checkSelectionRunnable = new Runnable() {
            @Override
            public void run() {
                checkSelection();
                handler.postDelayed(this, 500);
            }
        };
    }

    protected void loadLevel(int level) {
        switch (level) {
            case 1:
                textQuestion.setText("¿Cuál de las siguientes figuras es un triángulo?");
                option1.setImageResource(R.drawable.triangle);
                option1.setTag(R.drawable.triangle);

                option2.setImageResource(R.drawable.circulo);
                option2.setTag(R.drawable.circulo);

                option3.setImageResource(R.drawable.cuadrado);
                option3.setTag(R.drawable.cuadrado);

                correctDrawableId = R.drawable.triangle;
                break;

            case 2:
                textQuestion.setText("¿Cuál de las siguientes figuras es un cuadrado?");
                option1.setImageResource(R.drawable.triangle);
                option1.setTag(R.drawable.triangle);

                option2.setImageResource(R.drawable.cuadrado);
                option2.setTag(R.drawable.cuadrado);

                option3.setImageResource(R.drawable.rombo);
                option3.setTag(R.drawable.rombo);

                correctDrawableId = R.drawable.cuadrado;
                break;

            case 3:
                textQuestion.setText("¿Cuál de las siguientes figuras es un círculo?");
                option1.setImageResource(R.drawable.circulo);
                option1.setTag(R.drawable.circulo);

                option2.setImageResource(R.drawable.triangle);
                option2.setTag(R.drawable.triangle);

                option3.setImageResource(R.drawable.pentagono);
                option3.setTag(R.drawable.pentagono);

                correctDrawableId = R.drawable.circulo;
                break;

            case 4:
                textQuestion.setText("¿Cuál de las siguientes figuras es un rombo?");
                option1.setImageResource(R.drawable.pentagono);
                option1.setTag(R.drawable.pentagono);

                option2.setImageResource(R.drawable.rombo);
                option2.setTag(R.drawable.rombo);

                option3.setImageResource(R.drawable.cuadrado);
                option3.setTag(R.drawable.cuadrado);

                correctDrawableId = R.drawable.rombo;
                break;

            case 5:
                textQuestion.setText("¿Cuál de las siguientes figuras es un pentágono?");
                option1.setImageResource(R.drawable.triangle);
                option1.setTag(R.drawable.triangle);

                option2.setImageResource(R.drawable.hexagono);
                option2.setTag(R.drawable.hexagono);

                option3.setImageResource(R.drawable.pentagono);
                option3.setTag(R.drawable.pentagono);

                correctDrawableId = R.drawable.pentagono;
                break;

            default:
                textQuestion.setText("¡Has completado todos los niveles!");
                correctDrawableId = -1;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
        handler.post(checkSelectionRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(checkSelectionRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float x = event.values[0];
            float y = event.values[1];

            selectorX -= x * selectorSpeed;
            selectorY += y * selectorSpeed;

            selectorX = Math.max(0, Math.min(selectorX, screenWidth - selector.getWidth()));
            selectorY = Math.max(0, Math.min(selectorY, screenHeight - selector.getHeight()));

            selector.setX(selectorX);
            selector.setY(selectorY);
        }
    }

    private void checkSelection() {
        if (!canValidate) return;

        float selectorCenterX = selectorX + selector.getWidth() / 2f;
        float selectorCenterY = selectorY + selector.getHeight() / 2f;

        if (isOverlapping(selectorCenterX, selectorCenterY, option1)) {
            validateAnswer(option1);
        } else if (isOverlapping(selectorCenterX, selectorCenterY, option2)) {
            validateAnswer(option2);
        } else if (isOverlapping(selectorCenterX, selectorCenterY, option3)) {
            validateAnswer(option3);
        }
    }

    private boolean isOverlapping(float x, float y, ImageView option) {
        float optionLeft = option.getX();
        float optionRight = optionLeft + option.getWidth();
        float optionTop = option.getY();
        float optionBottom = optionTop + option.getHeight();

        return (x >= optionLeft && x <= optionRight && y >= optionTop && y <= optionBottom);
    }

    protected void validateAnswer(ImageView selectedOption) {
        if (correctDrawableId == -1 || !canValidate) return;

        canValidate = false;

        int selectedId = (int) selectedOption.getTag();

        if (selectedId == correctDrawableId) {
            selectedOption.setColorFilter(Color.GREEN);
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                int nextLevel = getIntent().getIntExtra("level", 1) + 1;
                if (nextLevel <= 5) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("level", nextLevel);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(MainActivity.this, Finish.class);
                    startActivity(intent);
                    finish();
                }
                canValidate = true;
            }, 1500);
        } else {
            selectedOption.setColorFilter(Color.RED);
            Toast.makeText(this, "Intenta de nuevo", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> {
                selectedOption.clearColorFilter();
                canValidate = true;
            }, 1500);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No usado
    }
}
