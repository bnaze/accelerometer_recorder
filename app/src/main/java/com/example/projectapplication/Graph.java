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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Graph extends AppCompatActivity implements SensorEventListener {

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int time;

    private boolean checkSensor = false;
    private Timer timer;
    private TimerTask timerTask;
    private Date date;

    private String fileRoot;
    private File dataFileDir;
    private File dataFile;
    private BufferedWriter writer;

    private String fileName = "datasample.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        fileRoot = Environment.getExternalStorageDirectory().toString();
        dataFileDir = new File(fileRoot);
        dataFile = new File(dataFileDir, fileName);

        date = new Date();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                checkSensor = true;
            }
        };

        //Change time to get sensor readings
        timer.schedule(timerTask,0,500);

        graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (checkSensor) {
            checkSensor = false;
            time++;
            try {
                DataPoint newData = new DataPoint(time, event.values[1]);
                series.appendData(newData, true, 40);
            } catch (Exception e) {
                Log.d("Exception1", e.getMessage());
            }

            //FileOutputStream fos = null;
            String entry = event.values[0] + "," + event.values[1] + "," + event.values[2] + "," + System.currentTimeMillis();

            try {
                writer = new BufferedWriter(new FileWriter(dataFile, true));
                writer.write(entry);
                writer.newLine();
                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
