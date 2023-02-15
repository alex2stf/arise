package com.arise.rapdroid.media.server;

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

        DeviceStat deviceStat = DeviceStat.getInstance();
        deviceStat.setProp(id + ".N", sensor.getName());
        deviceStat.setProp(id + ".P", String.valueOf(sensor.getPower()));
        deviceStat.setProp(id + ".V", String.valueOf(sensor.getVersion()));
        deviceStat.setProp(id + ".mD", String.valueOf(sensor.getMinDelay()));
        deviceStat.setProp(id + ".vd", sensor.getVendor() + "");
        deviceStat.setProp(id + ".T", sensor.getType() + "");
        deviceStat.setProp(id + ".TN", getTypeName(sensor.getType()) + "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            deviceStat.setProp(id + ".sT", String.valueOf(sensor.getStringType()));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            deviceStat.setProp(id + ".Md", String.valueOf(sensor.getMaxDelay()));
        }

        if (sensorEvent != null){

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sensorEvent.values.length; i++){
                if (i > 0){
                    sb.append(",");
                }
                sb.append(sensorEvent.values[i]);
            }

            deviceStat.setProp(id + ".A", String.valueOf(sensorEvent.accuracy));
            deviceStat.setProp(id + ".Ti", String.valueOf(sensorEvent.timestamp));
            deviceStat.setProp(id + ".L", sb.toString());
        }

        if (accuracy != null){
            deviceStat.setProp(id + ".A", String.valueOf(accuracy));
        }

    }

    private static String getTypeName(int n){
        switch (n){
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
        return n + "";
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
