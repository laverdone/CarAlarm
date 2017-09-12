package security.utils.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import  security.bean.GuardLog;
import security.utils.Const;
import security.utils.Logger;


import android.app.ProgressDialog;
import android.content.ContentValues;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

public class Repository extends DBAdapter{
    private Context mContext;

    public Repository(Context context) {
        super(context);
        mContext=context;
    }

    /**
     * Scrive il log nella tabella
     *
     * */
	public synchronized void guardLoggger(String logType,
			String logDesc, double latitude, double longitude, double altitude) {
        final String mLogType=logType;
        final String mLogDesc=logDesc;
        final double mLatitude=latitude;
        final double mLongitude=longitude;
        final double mAltitude=altitude;
        if(Const.IS_DEBUG)  Logger.log(mLogType+" "+mLogDesc+" Longitude: "+mLongitude+" Latidute: "+mLatitude+" Altitude: "+mAltitude);


        new Thread(new Runnable() {
            @Override
            public void run() {
                open();

                ContentValues tableLogValue = new ContentValues();
                tableLogValue.put(CarAlarmGuardTable.LOGTYPE, mLogType);
                tableLogValue.put(CarAlarmGuardTable.LOGDESC, mLogDesc);
                tableLogValue.put(CarAlarmGuardTable.LOGLONGITUDE, mLongitude);
                tableLogValue.put(CarAlarmGuardTable.LOGLATIDUTE, mLatitude);
                tableLogValue.put(CarAlarmGuardTable.LOGALTIDUTE, mAltitude);


                //Log.v("Repository", "contentValues settato");

                try {
                    // Log.v("Classe Repository", "Sta per fare la query");

                    getmDb().insertOrThrow(CarAlarmGuardTable.TABLE_NAME, null, tableLogValue);

                } catch (SQLiteException e) {
                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "SQL Error logging event");
                    close();

                }catch (IllegalStateException e){
                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "IllegalStateException Error logging event");
                    close();
                }
                close();
            }
        }).start();


	}

    /**
     * Scrive il log nella tabella
     *
     * */
    public synchronized void putPref(String pref,
                                          boolean value) {
        final String mPref=pref;
        final boolean mValue=value;

        new Thread(new Runnable() {
            @Override
            public void run() {
                open();

                ContentValues tableLogValue = new ContentValues();
                tableLogValue.put(CarAlarmPrefTable.PREFS, mPref);
                tableLogValue.put(CarAlarmPrefTable.VALUE, mValue);
                try {
                    getmDb().insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);
                } catch (SQLiteException e) {
                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "SQL Error logging event");
                    close();
                }catch (IllegalStateException e){
                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "IllegalStateException Error logging event");
                    close();
                }
                close();
            }
        }).start();

    }

    /**
     * Carica i dettagli del diary
     *
     * */
    public boolean getPref(String pref) {
        int iValue=0;
        boolean mReturn=true;
        Cursor oCursor = null;
        open();
        oCursor = getmDb().query(CarAlarmPrefTable.TABLE_NAME, CarAlarmPrefTable.COLUMNS, CarAlarmPrefTable.PREFS+"='"+pref+"'"
                    , null, null , null, null);

        while(oCursor != null && oCursor.moveToNext()) {
            iValue=oCursor.getInt((oCursor.getColumnIndex(CarAlarmPrefTable.VALUE)));
            if(iValue==0) mReturn=false;
            else mReturn=true;
        }
        if(oCursor!=null) oCursor.close();
        close();
        return mReturn;
    }

    /**
     * Carica i dettagli del diary
     *
     * */
    public synchronized HashMap<String, Boolean> getAllPref() {
        int iValue=0;
        HashMap<String, Boolean> mAllPrefs= new HashMap<String, Boolean>();
        Cursor oCursor = null;
        open();
        try {
            oCursor = getmDb().query(CarAlarmPrefTable.TABLE_NAME, CarAlarmPrefTable.COLUMNS, null
                    , null, null , null, null);

            while(oCursor != null && oCursor.moveToNext()) {
                iValue=oCursor.getInt((oCursor.getColumnIndex(CarAlarmPrefTable.VALUE)));

                if(iValue==0){
                    mAllPrefs.put(oCursor.getString((oCursor.getColumnIndex(CarAlarmPrefTable.PREFS))),
                            false);
                }else{
                    mAllPrefs.put(oCursor.getString((oCursor.getColumnIndex(CarAlarmPrefTable.PREFS))),
                            true);
                }
            }
        } catch (SQLiteException e) {
            if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "SQL Error logging event");
            if(oCursor!=null) oCursor.close();
            close();
        }catch (IllegalStateException e){
            if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "IllegalStateException Error logging event");
            if(oCursor!=null) oCursor.close();
            close();
        }
        if(oCursor!=null) oCursor.close();
        close();
        getmDb().close();
        return mAllPrefs;
    }

	/**
	 * Carica i dettagli del diary
	 *
	 * */
	public ArrayList<GuardLog> loadGuardLog(String filter) {
        ArrayList<GuardLog> mLogEvent = new ArrayList<GuardLog>();
        Cursor oCursor = null;
        open();

        if(filter!=null){
            oCursor = getmDb().query(CarAlarmGuardTable.TABLE_NAME, CarAlarmGuardTable.COLUMNS, CarAlarmGuardTable.LOGTYPE+"='"+filter+"'"
                    , null, null , null, CarAlarmGuardTable.LOGID+" DESC");
        }else{
            oCursor = getmDb().query(CarAlarmGuardTable.TABLE_NAME, CarAlarmGuardTable.COLUMNS, null
                    , null, null , null, CarAlarmGuardTable.LOGID+" DESC");
        }


		while(oCursor != null && oCursor.moveToNext()) {

            SimpleDateFormat iso8601Format = new SimpleDateFormat(
		            "yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		    GuardLog tmpEvent = new GuardLog();
            tmpEvent.logDESC=oCursor.getString(oCursor.getColumnIndex(CarAlarmGuardTable.LOGDESC));
            tmpEvent.logTYPE=oCursor.getString(oCursor.getColumnIndex(CarAlarmGuardTable.LOGTYPE));
            try {
                tmpEvent.logDATE=iso8601Format.parse(oCursor.getString(oCursor.getColumnIndex(CarAlarmGuardTable.LOGDATE)));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            mLogEvent.add(tmpEvent);
            //TODO
		}
		if(oCursor!=null) oCursor.close();

        close();
		return mLogEvent;
	}
    /**
     * Cancella tutti i Log
     *
     * */
    public void deleteGuardLog(String filter) {
        open();
        if(filter!=null){
            getmDb().delete(CarAlarmGuardTable.TABLE_NAME,CarAlarmGuardTable.LOGTYPE+"='"+filter+"'",null);
        }else{
            getmDb().delete(CarAlarmGuardTable.TABLE_NAME,CarAlarmGuardTable.LOGTYPE+"='"+filter+"'",null);
        }
        close();

    }
}