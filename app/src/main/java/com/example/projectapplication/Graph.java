package com.example.projectapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Vibrator;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Graph extends AppCompatActivity implements SensorEventListener {

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private SensorManager sensorManager;
    private Sensor sensor;

    private boolean record = false;

    private String fileRoot;
    private File dataFileDir;
    private File dataFile;
    private BufferedWriter writer;

    private float dx = 0;
    private float dy = 0;
    private float dz = 0;

    private float lastX;
    private float lastY;
    private float lastZ;

    private TextView currentX;
    private TextView currentY;
    private TextView currentZ;

    private float vibrateThreshold;
    private Vibrator vibrator;

    private Button recordButton;
    private Button deleteButton;

    private String fileName = "datasample.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //Assign file variables used to create files for analysis
        fileRoot = Environment.getExternalStorageDirectory().toString();
        dataFileDir = new File(fileRoot);
        dataFile = new File(dataFileDir, fileName);

        //Create sensors here
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        //initialise views
        currentX = findViewById(R.id.currentX);
        currentY = findViewById(R.id.currentY);
        currentZ = findViewById(R.id.currentZ);

        vibrateThreshold = sensor.getMaximumRange() / 2;
        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        recordButton = findViewById(R.id.record);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record == false) {
                    recordButton.setText("Stop Recording");
                    record = true;
                }
                else{
                    recordButton.setText(("Record"));
                    record = false;
                }
            }
        });

        deleteButton = findViewById(R.id.deleteExisting);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataFile.exists()){
                    dataFile.delete();
                    dataFile = new File(dataFileDir, fileName);
                }
            }
        });
    }

    public void resetDisplay(){
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    public void displayReadings(){
        currentX.setText(Float.toString(dx));
        currentY.setText(Float.toString(dy));
        currentZ.setText(Float.toString(dz));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        resetDisplay();

        displayReadings();

        dx = Math.abs(lastX - event.values[0]);
        dy = Math.abs(lastY - event.values[1]);
        dz = Math.abs(lastZ - event.values[2]);

        /*
        if(event.values[0] < 0){
            dx = event.values[0];
        }
        else if(event.values[1] < 0){
            dy = event.values[1];
        }
        else if(event.values[2] < 0){
            dz = event.values[2];
        }
         */

        if (dx < 2)
            dx = 0;
        if (dy < 2)
            dy = 0;
        if (dz < 2){
            dz = 0;
        }
    if (dz > vibrateThreshold || (dy > vibrateThreshold) || (dx > vibrateThreshold)) {
            vibrator.vibrate(50);
        }

        String entry = event.values[0] + "," + event.values[1] + "," + System.currentTimeMillis();

        if(record) {
            try {
                writer = new BufferedWriter(new FileWriter(dataFile, true));
                writer.write(entry);
                writer.newLine();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        vibrate();
    }

    public void vibrate() {
        if ((dx > vibrateThreshold) || (dy > vibrateThreshold) || (dz > vibrateThreshold)) {
            vibrator.vibrate(50);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
