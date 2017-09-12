package security.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import security.car.alarm.MainActivity;
import security.car.alarm.R;
import security.utils.Const;
import security.utils.Logger;
import security.utils.db.Repository;
import security.utils.sensor.AccelerometerHelper;
import security.utils.sensor.BlueToothHelper;



public class CarAlarmGuard extends Service implements LocationListener {


    /**true is user stop service*/
    private boolean isKillExplicit = false;

    public int mStatus = 2;

    private AccelerometerHelper mAccelerometer = null;
    private Repository mRepository = null;
    private BlueToothHelper mBluetoothHelper;

    private static Context mContext;

    private PendingIntent contentIntent;
    private Notification notification;
    private String sPid = "0";

    private Thread mThreadPrefs = new Thread(new PrefsAsynkTask());


    /**Avvio ms del Timer per le coordinate e salvare il tutto in DB*/
    private static final int START_TIMER_DELAY = 0;
    /**Intervallo ms Periodico del Timer per le coordinate e salvare il tutto in DB*/
    private static final int PERIOD_TIMER_DELAY = 0;
    /**Periodo di lettura accelerometro*/
    private static final int PERIOD_ACCELEROMETER_TIMER = 1000;

    private static final int GEOCODER_MAX_RESULTS = 5;
    private Timer mTimerBlueTooth = null;
    private LocationManager mLocationManager = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double pre_latitude = 0.0;
    private double pre_longitude = 0.0;
    private long altitude = 0;
    private long pre_altitude = 0;

    private SharedPreferences prefs;

    private Timer SMSAlarmTimer = null;
    private int mNumberOfSMSSended = 0;
    private boolean isArmated = false;
    private boolean isRunning = false;
    private static final int gpsMinDistance = 50;


    private boolean isBluetooth=false;
    private boolean isSMS=false;
    private boolean isAccererometer=false;
    private boolean isGPS=false;
    private boolean isService=false;
    private boolean isCall=false;
    private HashMap<String, Boolean> mPrefs = null;


    public CarAlarmGuard() {
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mRepository = new Repository(getApplicationContext());
        mPrefs = mRepository.getAllPref();

        SimpleDateFormat sdf = new SimpleDateFormat("ssSSS");
        sPid = sdf.format(new Date());

        new Thread(new StartAlarm(true)).start();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getClass().getCanonicalName(), "Car Alarm Service Start");
        if(intent!=null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.getBoolean("forceStart", false)) {
                    mStatus = Const.STATUS_STOP;
                }
                if (extras.getBoolean("activate", false)) {
                    activateAlarm();
                }else if (!extras.getBoolean("activate", false)) {
                    deActivateAlarm();
                }
            }
        }
        new Thread(new StartAlarm(true)).start();
        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isArmated) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = Math.round(location.getAltitude());
            if (pre_latitude == 0 && pre_longitude == 0) {
                pre_latitude = latitude;
                pre_longitude = longitude;
                if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "Alarm Activate at position: " + latitude + " - " + longitude);
                if(Const.IS_DEBUG)  Logger.log("Alarm Activate at position: " + latitude + " - " + longitude);

                Toast.makeText(getApplicationContext(), "Location Alarm Activate at position: " + latitude + " - " + longitude,
                        Toast.LENGTH_SHORT).show();
            }

            //Lettuta alla notification dei GPS
            if (longitude != 0.0 && latitude != 0.0) {
                if (pre_latitude != latitude && pre_longitude != longitude) {
                    Log.i(this.getClass().getCanonicalName(), "Alarm RING: " + latitude + " - " + longitude);
                    //Toast.makeText(getApplicationContext(), "Alarm RING: "+latitude+" - "+longitude,
                    //        Toast.LENGTH_LONG).show();
                    mRepository.guardLoggger("ALERT", "Car Alarm Guard Service ALARM Sensor GPS ", latitude, longitude, altitude);
                    if (SMSAlarmTimer == null) {
                        SMSAlarmTimer = new Timer();
                        SMSAlarmTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                sendAlarm("GPS");
                                sendCallAlarm();
                            }
                        }, Const.DELAY_ALARM_START, Const.REPEAT_ALARM);
                    }
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /**
     * Show a notification while this service is running.
     *
     */
    private Notification showNotification(int iIcon, CharSequence charSequence) {
        //Controllo la configurazione dal DB
        //Con lo startforeground la notidicha è sempre necessaria
        //if(!oConfigTrainer.isbDisplayNotification()) return null;
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.app_name);


        // Set the icon, scrolling text and timestamp
        notification = new Notification(); //new Notification(iIcon, text,
        contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = new Notification.Builder(mContext)
                        .setContentTitle(text)
                        .setContentText(charSequence)
                        .setSmallIcon(iIcon).setContentIntent(contentIntent)
                        .build();

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(Integer.parseInt(sPid), notification);
            }
        }

        /*//System.currentTimeMillis());
        notification.icon=iIcon;
        notification.tickerText=charSequence;
        contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(getApplicationContext(), MainActivity.class), 0);

        //notification.contentIntent=contentIntent;
        //notification.contentIntent=contentIntent;
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, charSequence,
                text, contentIntent);*/

        // Send the notification.
        //if(mNM!=null) mNM.notify(NOTIFICATION, notification);
        return notification;
    }

    public void startBluetoothServer() {
        Log.v(this.getClass().getCanonicalName(), "Start Bluetooth service");
        //Verifico se è abilitato sull'ACCELEROMETRO start
        if(isBluetooth){
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(enableBtIntent);
            }
        }

        if (mBluetoothHelper == null) {
            mBluetoothHelper = new BlueToothHelper(mContext, this);
        }
        if (Const.IS_DEBUG)
            Log.v(this.getClass().getCanonicalName(), "mBluetoothHelper.isBluetoothAvail(): " + mBluetoothHelper.isBluetoothAvail()
                    + " mBluetoothHelper.mState: " + mBluetoothHelper.mState);

        if (mBluetoothHelper.isBluetoothAvail() && mBluetoothHelper.mState == mBluetoothHelper.STATE_NONE)
            mBluetoothHelper.waitForConnect();
        if (mBluetoothHelper.isBluetoothAvail() && mBluetoothHelper.mState != mBluetoothHelper.STATE_NONE){
            mBluetoothHelper = new BlueToothHelper(mContext, this);
            mBluetoothHelper.waitForConnect();
        }

    }

    public void stopBluetoothService() {
        if (mTimerBlueTooth != null) {
            mTimerBlueTooth.cancel();
        }
        if (mBluetoothHelper != null) {
            mBluetoothHelper.stop();
        }
    }

    public void stopAccelerometer() {
        if (mAccelerometer != null) {
            mAccelerometer.stopListening();
        }
    }

    public void stopGps() {
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.removeUpdates(this);
        }
    }

    public void endCarAlarmService() {
        if (SMSAlarmTimer != null) {
            SMSAlarmTimer.cancel();
            SMSAlarmTimer = null;
        }
        stopAccelerometer();
        stopBluetoothService();
        stopGps();
    }

    /**
     * Avvia il controllo dell'accelerometro
     *
     * */
    private void startAccelerometer() {
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            return;
        }
        mAccelerometer = new AccelerometerHelper(mContext, this);
        mRepository.guardLoggger("INFO", "Car Alarm Guard Service Start Accelerometer Sensor", latitude, longitude, altitude);
    }

    /**
     * Avvio il FIX del GPS
     * @throws Throwable
     * */
    private void startLocationFix() {
    	/*if(LocationManager!=null){
    		LocationManager.removeUpdates(this);
    		LocationManager=null;
    	}*/
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!isGPS) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, PERIOD_TIMER_DELAY, gpsMinDistance, this, Looper.getMainLooper());
                mRepository.guardLoggger("INFO", "Car Alarm Guard Service Start GPS Sensor for calculate location", latitude, longitude, altitude);
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mLocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, PERIOD_TIMER_DELAY, gpsMinDistance, this, Looper.getMainLooper());
                mRepository.guardLoggger("INFO", "Car Alarm Guard Service Start NETWORK Sensor for calculate location", latitude, longitude, altitude);
            }
            if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "Add GPS Fix");

        } catch (RuntimeException e) {
            if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "Errore GPS fix");
            //endAllListner();
            e.printStackTrace();
            try {
                this.finalize();
            } catch (Throwable e1) {
                if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "End GPS Fix");
                System.exit(0);
            }
        }
    }


    /**
     * ESEGUE UN CHECK DELL'ALLARME
     * */
    public void testAlarm() {
        if(!mThreadPrefs.isAlive()){
            mThreadPrefs = new Thread(new PrefsAsynkTask());
            mThreadPrefs.start();
        }


        resetAlarm();
        isArmated = true;
        resetAlarm();

        if (isBluetooth) {
            startBluetoothServer();
        }

        if (isSMS) {
            if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "Sending Test SMS");
            mRepository.guardLoggger("INFO", "Car Alarm Guard Service Sending Test SMS", latitude, longitude, altitude);
            sendAlarm("dummy");
        }
        startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_activated, getString(R.string.testAlarm)));

        mStatus = Const.STATUS_TESTING;
    }

    /**
     * ABILITA l'ALLARME
     * TODO aggiundere il delay di 30Sec/1min prima di mettersi in armato
     * */
    public void activateAlarm() {
        if(!mThreadPrefs.isAlive()){
            mThreadPrefs = new Thread(new PrefsAsynkTask());
            mThreadPrefs.start();
        }

        resetAlarm();
        isArmated = true;
        //Verifico se è abilitato sull'ACCELEROMETRO start
        startLocationFix();


        //Verifico se è abilitato sull'ACCELEROMETRO start
        if (isAccererometer) {
            startAccelerometer();
        }

        if (isBluetooth) {
            startBluetoothServer();
        }

        if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "Alarm Activate!");
        startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_activated, getString(R.string.activateAlarm)));

        MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.activate);
        player.start();


        mStatus = Const.STATUS_RUNNING;
    }

    /**
     * Qua resetto tutti i valori dell'allarme
     *
     * */
    private void resetAlarm() {

        mRepository.guardLoggger("INFO", "Car Alarm Guard Service reset Parameter", latitude, longitude, altitude);
        latitude = 0;
        longitude = 0;
        altitude = 0;
        pre_longitude = 0;
        pre_latitude = 0;
        pre_altitude = 0;
        if (SMSAlarmTimer != null) {
            SMSAlarmTimer.cancel();
            SMSAlarmTimer = null;
        }
        if (mAccelerometer != null) {
            mAccelerometer.stopListening();
        }
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
    }

    /**
     * DISABILITA L'ALLARME
     * */
    public void deActivateAlarm() {
        if(!mThreadPrefs.isAlive()){
            mThreadPrefs = new Thread(new PrefsAsynkTask());
            mThreadPrefs.start();
        }

        resetAlarm();
        isArmated = false;
        startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_deactivated, getString(R.string.deactivateAlarm)));
        mRepository.guardLoggger("INFO", "Car Alarm Guard Service deactivate", latitude, longitude, altitude);

        if (isBluetooth) {
            startBluetoothServer();
        }

        MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.deactivate);
        player.start();

        mStatus = Const.STATUS_STOP;
    }


    public void sendAccelerometerAlarm() {
        if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "Accelerometer alarm!!!");
        mRepository.guardLoggger("ALERT", "Alarm Activate by Sensor Accelerometer", latitude, longitude, altitude);
        if (SMSAlarmTimer == null) {
            SMSAlarmTimer = new Timer();
            SMSAlarmTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sendAlarm("accelerometer");
                    sendCallAlarm();

                }
            }, Const.DELAY_ALARM_START, Const.REPEAT_ALARM);
        }

    }


    /**
     * invia un SMS al numero impostato con le coordinate e il tipo di allarme scattato
     * */
    private void sendAlarm(String sensor) {
        if (!isSMS) {
            if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(), "Alarm with sensor " + sensor + " but no send SMS");
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        String sPhone1 = prefs.getString("phone1", "");
        String sPhone2 = prefs.getString("phone2", "");
        String sSmsMessage = prefs.getString("smsText", "");
        if (sPhone1.trim().length() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_DENIED) {
                    mRepository.guardLoggger("ERR", "permission denied to SEND_SMS " + sPhone1, latitude, longitude, altitude);

                }
            } else {
                smsManager.sendTextMessage(sPhone1, null, sSmsMessage + " Acttual location is https://maps.google.com/maps?q=" + latitude + "," + longitude + "&z=18", null, null);
                mRepository.guardLoggger("WARN", "Alarm Send message to " + sPhone1, latitude, longitude, altitude);
            }
        }
        if (sPhone2.trim().length() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_DENIED) {
                    mRepository.guardLoggger("ERR", "permission denied to SEND_SMS " + sPhone1, latitude, longitude, altitude);

                }
            } else {
                smsManager.sendTextMessage(sPhone2, null, sSmsMessage + " Acttual location is https://maps.google.com/maps?q=" + latitude + "," + longitude + "&z=18", null, null);
                mRepository.guardLoggger("WARN", "Alarm Send message to " + sPhone2, latitude, longitude, altitude);
            }
        }

    }

    private void sendCallAlarm() {
        if (!isCall) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        String sPhone1 = prefs.getString("callphone1", "");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + sPhone1));

        mRepository.guardLoggger("WARN", "Alarm make call to " + sPhone1, latitude, longitude, altitude);
        if(sPhone1.length()>0) mContext.startActivity(intent);
    }

    class PrefsAsynkTask implements Runnable{

        @Override
        public void run() {
            /*prefs=null;
            prefs = getSharedPreferences(Const.SHARED_PREFS, Context.MODE_PRIVATE);
            prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"shared mod key is: "+key+" value is: "+sharedPreferences.getBoolean(key,false));
                }
            });*/
            try{
                mPrefs = mRepository.getAllPref();
                isBluetooth     =   mPrefs.get("bluetooth");
                isSMS           =   mPrefs.get("sms");
                isAccererometer =   mPrefs.get("accererometer");
                isGPS           =   mPrefs.get("gps");
                isService       =   mPrefs.get("service");
                isCall          =   mPrefs.get("call");
            }catch (IllegalStateException e){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"IllegalStateException Car Alarm Service Start not reload config");
            }catch (NullPointerException e1){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"NullPointerException not reload config");
            }

           /* if(Const.IS_DEBUG){
                Log.v(this.getClass().getCanonicalName(),"isBluetooth: "+prefs.getBoolean("bluetooth",true));
                Log.v(this.getClass().getCanonicalName(),"isBluetooth DB: "+mRepository.getPref("bluetooth"));
                Log.v(this.getClass().getCanonicalName(),"isSMS: "+prefs.getBoolean("sms",true));
                Log.v(this.getClass().getCanonicalName(),"isSMS DB: "+mRepository.getPref("sms"));
                Log.v(this.getClass().getCanonicalName(),"isAccererometer: "+prefs.getBoolean("accererometer",true));
                Log.v(this.getClass().getCanonicalName(),"isAccererometer DB: "+mRepository.getPref("accererometer"));
                Log.v(this.getClass().getCanonicalName(),"isGPS: "+prefs.getBoolean("gps",true));
                Log.v(this.getClass().getCanonicalName(),"isGPS DB: "+mRepository.getPref("gps"));
                Log.v(this.getClass().getCanonicalName(),"isService: "+prefs.getBoolean("service",true));
                Log.v(this.getClass().getCanonicalName(),"isService DB: "+mRepository.getPref("service"));

            }*/
//            if(prefs.getBoolean("bluetooth",true)){
//                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    getApplicationContext().startActivity(enableBtIntent);
//
//                }else{
//                    startBluetoothServer();
//                }
//            }
            if(mTimerBlueTooth==null){
                mTimerBlueTooth = new Timer();
                mTimerBlueTooth.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(isBluetooth
                                && mBluetoothHelper!=null
                                && mBluetoothHelper.mState==mBluetoothHelper.STATE_ERROR){
                            mBluetoothHelper.stop();
                            mBluetoothHelper=null;
                            startBluetoothServer();
                        }
                    }
                },Const.DELAY_BLUETOOTH,Const.PERIOD_BLUETOOTH);
            }
            Thread.currentThread().interrupt();
        }
    }

    class StartAlarm implements Runnable {
        private boolean mFirstLaunch=true;

        public StartAlarm(boolean firtlaunch) {
            mFirstLaunch=firtlaunch;
        }

        @Override
        public void run() {


            //mFirstLaunch=firstLaunch;
            prefs = mContext.getSharedPreferences(Const.SHARED_PREFS, Context.MODE_PRIVATE);


            try{
                mPrefs = mRepository.getAllPref();
                isBluetooth     =   mPrefs.get("bluetooth");
                isSMS           =   mPrefs.get("sms");
                isAccererometer =   mPrefs.get("accererometer");
                isGPS           =   mPrefs.get("gps");
                isService       =   mPrefs.get("service");
                isCall          =   mPrefs.get("call");
            }catch (IllegalStateException e){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"IllegalStateException Car Alarm Service Start not reload config");
            }catch (NullPointerException e1){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"NullPointerException not reload config");
            }

            if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"start on Boot is: "+prefs.getBoolean("service",false));
            //Verifico se è abilitato sull'on start
            /*if(!prefs.getBoolean("service",false)){
                isRunning=false;


                Log.v(this.getClass().getCanonicalName(),"Car Guard start on BOOT DISABLED");
                isKillExplicit=true;
                onDestroy();
            }else{*/
            isRunning=true;
            if(mStatus==Const.STATUS_RUNNING){
                startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_activated, getString(R.string.deactivateAlarm)));
            }else if (mStatus==Const.STATUS_STOP) {
                startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_deactivated, getString(R.string.deactivateAlarm)));
            }else if (mStatus==Const.STATUS_ENDED){
                stopSelf();
            }
            //}
            try {
                Looper.getMainLooper();
                Looper.prepareMainLooper();
            }catch (IllegalStateException e){
                if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(),"Looper prepare not neded");
            }
            //Verifico se è abilitato sull'ACCELEROMETRO start
            if(mFirstLaunch && isBluetooth){
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(enableBtIntent);

                }else{
                    startBluetoothServer();
                }
            }
            //TODO inserire il controllo del bluetooth
            if(mTimerBlueTooth==null){
                mTimerBlueTooth = new Timer();
                mTimerBlueTooth.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(isBluetooth
                                && mBluetoothHelper!=null
                                    && mBluetoothHelper.mState==mBluetoothHelper.STATE_ERROR){
                            mBluetoothHelper.stop();
                            mBluetoothHelper=null;
                            startBluetoothServer();
                        }
                    }
                },Const.DELAY_BLUETOOTH,Const.PERIOD_BLUETOOTH);
            }

        }
    }

    @Override
    public void onDestroy() {
        if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"Destroy Services");
        mRepository.guardLoggger("WARN","Car Alarm Guard Service Destroy",latitude,longitude,altitude);
        if(isKillExplicit){
            if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"Destroy Services isKillExplicit: "+isKillExplicit+" stopSelf();");
            endCarAlarmService();
            mStatus=Const.STATUS_ENDED;
            stopForeground(true);
            stopSelf();
            return;
        }

        if(prefs.getBoolean("service", false)){
            if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"Destroy Services restart Service service param: "+prefs.getBoolean("service", false)+" ");
            startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_deactivated, getString(R.string.app_name)));
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(), "Running on Low Memory");
        mRepository.guardLoggger("WARN","Car Alarm Guard Service Running on Low Memory",latitude,longitude,altitude);
        super.onLowMemory();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        if(Const.IS_DEBUG) Log.i(this.getClass().getCanonicalName(), "onBind->Services");
        return mBinder;
        //return mMessenger.getBinder();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        //onDestroy();
        //isRunning=false;
        return super.onUnbind(intent);
    }

    private final ICarAlarmGuard.Stub mBinder = new ICarAlarmGuard.Stub(){

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getPid() throws RemoteException {
            return null;
        }

        @Override
        public boolean startAlarm() throws RemoteException {
            return false;
        }

        @Override
        public boolean stopAlarm() throws RemoteException {
            return false;
        }

        @Override
        public boolean setAlarmOn() throws RemoteException {
            activateAlarm();
            return false;
        }

        @Override
        public boolean setAlarmOff() throws RemoteException {
            deActivateAlarm();
            return false;
        }


        @Override
        public boolean setAlarmTest() throws RemoteException {
            testAlarm();
            return false;
        }

        @Override
        public boolean isRunning() throws RemoteException {
            return isRunning;
        }

        @Override
        public boolean isAlarmActivated() throws RemoteException {
            return isArmated;
        }

        @Override
        public double getAltitude() throws RemoteException {
            return 0;
        }

        @Override
        public double getLatidute() throws RemoteException {
            return latitude;
        }

        @Override
        public double getLongitude() throws RemoteException {
            return longitude;
        }

        @Override
        public void stopGPSFix() throws RemoteException {
            stopAlarm();
        }

        @Override
        public void shutDown() throws RemoteException {
            isKillExplicit=true;
            onDestroy();
        }

        @Override
        public boolean isGPSFixPosition() throws RemoteException {
            return false;
        }

        @Override
        public int getStatus() throws RemoteException {
            return mStatus;
        }

        @Override
        public boolean setAsService() throws RemoteException {
            startForeground(Integer.parseInt(sPid), showNotification(R.drawable.alarm_deactivated, getString(R.string.app_name)));
            return true;
        }
        public void reloadConfig(){
            if(!mThreadPrefs.isAlive()){
                mThreadPrefs = new Thread(new PrefsAsynkTask());
                mThreadPrefs.start();
            }
        }
    };
}
