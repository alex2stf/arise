package com.arise.droid.impl;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import com.arise.weland.dto.DeviceStat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SensorReader implements SensorEventListener {
    private static final AtomicInteger counter = new AtomicInteger();
    private static final Map<String, Integer> ids = new ConcurrentHashMap<>();
    private final SensorManager sensorManager;

    public SensorReader(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public static void applyEventListener(SensorManager sensorManager, SensorEventListener sensorEventListener) {
       List<Sensor> sensorsList = sensorManager.getSensorList(Sensor.TYPE_ALL);

       DeviceStat.getInstance().setProp("_s_total", sensorsList.size() + "");
       for (Sensor s: sensorsList){
           readSensor(s, null, null);
           sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_NORMAL);

       }

    }

    private static final int getId(String s){
        if (ids.containsKey(s)){
            return ids.get(s);
        }
        ids.put(s, counter.incrementAndGet());
        return ids.get(s);
    }

    private static void readSensor(Sensor sensor, SensorEvent sensorEvent, Integer accuracy){
        String id = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            id += String.valueOf(sensor.getId());
        }

        id += (sensor.getType() + sensor.getName()).replaceAll("\\s+", "");
        id = "_s." + getId(id);

                //StringEncoder.MESSAGE_DIGEST.encode((sensor.getName() + sensor.getVendor() + sensor.getVersion()), "MD5");

            DeviceStat.getInstance()
                    .setSysProp(
                            id,
                            sensor.getName(),
                            "version=" + sensor.getVersion() +
                                    "&type=" + getTypeName(sensor) +
                                    "&power=" + sensor.getPower() +
                                    "&minDelay=" + sensor.getMinDelay() +
                                    ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ? "&maxDelay=" + sensor.getMaxDelay() : "" ) +
                                    "&vendor="+ sensor.getVendor(),
                            accuracy != null ? accuracy + "" : "N/A"
                    );


        if (sensorEvent != null){

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sensorEvent.values.length; i++){
                if (i > 0){
                    sb.append(",");
                }
                sb.append(sensorEvent.values[i]);
            }

            DeviceStat.getInstance().setSysProp(
                    id + ".E",
                    sensorEvent.sensor.getName() + " event",
                    "timestamp=" + sensorEvent.timestamp + "&accuracy=" + sensorEvent.accuracy,
                    sb.toString()
            );
        }
    }

    private static String getTypeName(Sensor sensor){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return String.valueOf(sensor.getStringType());
        }
        switch (sensor.getType()){
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "TYPE_AMBIENT_TEMPERATURE";

            case Sensor.TYPE_TEMPERATURE:
                return "TYPE_TEMPERATURE"; //event.values[0]	°C	Device temperature.1

            case Sensor.TYPE_LIGHT:
                return "TYPE_LIGHT"; //Illuminance.

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "TYPE_RELATIVE_HUMIDITY"; //event.values[0]	%	Ambient relative humidity.

            case Sensor.TYPE_PRESSURE:
                return "TYPE_PRESSURE"; //event.values[0]	hPa or mbar	Ambient air pressure.
            case Sensor.TYPE_ACCELEROMETER:
                return "TYPE_ACCELEROMETER";
            case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
                return "TYPE_ACCELEROMETER_UNCALIBRATED";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "TYPE_MAGNETIC_FIELD";
        }
        return sensor.getType() + "";
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        readSensor(sensorEvent.sensor, sensorEvent, null);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        readSensor(sensor, null, i);
    }
}
