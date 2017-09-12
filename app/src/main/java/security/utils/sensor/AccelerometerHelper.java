package security.utils.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import security.service.CarAlarmGuard;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gianluca on 12/09/13.
 */
public class AccelerometerHelper implements SensorEventListener {

    /** True when the Accelerometer-functionality is basically available. */
    boolean accelerometerAvailable = false;
    boolean isEnabled = false;
    private SensorManager mSensorManager=null;
    private Sensor mAccelerometer=null;
    private Context mContext=null;
    private CarAlarmGuard mService=null;
    private Timer mTimerCalibration=null;
    private boolean isCalibrate=false;
    //Parametri di auto calibrazione
    private float mZCalibrate=0f;
    private float mYCalibrate=0f;
    private float mXCalibrate=0f;
    /**
     * Sets up an AccelerometerReader. Checks if Accelerometer is available on
     * this device and throws UnsupportedOperationException if not .
     *
     */
    public AccelerometerHelper(Context context, CarAlarmGuard service)
            throws UnsupportedOperationException {
        mContext=context;
        mService=service;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(this.getClass().getCanonicalName(), "Using Accelerometer Sensor ");
        //Lancio il timer di calibrazione dopo 30 sec di attivazione allarme
        mTimerCalibration = new Timer();
        mTimerCalibration.schedule(new TimerTask() {
            @Override
            public void run() {
                isCalibrate=true;
                Log.v(this.getClass().getCanonicalName(), "Accelerometer Calibrate AT Z: "+mZCalibrate+" Y: "+mYCalibrate+" X: "+mXCalibrate);
            }
        },30000);
    }
    /**
     * Imposta la sensibilità del sensore
     *
     * */
    public void setAccuracy(int sensorAccuracy){
        mSensorManager.unregisterListener(this);
        mSensorManager.registerListener(this, mAccelerometer, sensorAccuracy);
    }

    /**
     * Imposta la sensibilità del sensore
     *
     * */
    public void stopListening(){
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if(!isCalibrate){
            mZCalibrate=z;
            mYCalibrate=y;
            mXCalibrate=x;
        }else{
            if(Math.round((mXCalibrate-x)*100)>100 ||
                 Math.round((mYCalibrate-y)*100)>100 ||
                    Math.round((mZCalibrate-z)*100)>100){
                Log.v(this.getClass().getCanonicalName(), "Accelerometer Sensor Changed: x%="+Math.round((mXCalibrate-x)*100));
                Log.v(this.getClass().getCanonicalName(), "Accelerometer Sensor Changed: y%="+Math.round((mYCalibrate-y)*100));
                Log.v(this.getClass().getCanonicalName(), "Accelerometer Sensor Changed: z%="+Math.round((mZCalibrate-z)*100));

                //TODO sendSMS
                mService.sendAccelerometerAlarm();
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
