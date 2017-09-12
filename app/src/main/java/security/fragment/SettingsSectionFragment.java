package security.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import security.car.alarm.R;
import security.service.CarAlarmGuard;
import security.utils.CarAlarmServiceConnection;
import security.utils.Const;
import security.utils.db.Repository;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class SettingsSectionFragment extends Fragment {

    private Context mContext;
    private SharedPreferences prefs;
    private CheckBox mCheckService;
    private CheckBox mCheckGPS;
    private CheckBox mCheckSMS;
    private CheckBox mCheckCall;
    private CheckBox mCheckSMSRemoteControl;
    private CheckBox mCheckAcc;
    private CheckBox mCheckBluetooth;
    private ImageButton mBtnPhoneDetails;
    private ImageButton mBtnPhoneDetailsCall;
    private ImageButton mBtnSMSPhoneForRemoteControl;
    private ToggleButton mStartStopService;
    private CarAlarmServiceConnection mConnection = null;
    private Repository mRepository=null;

    public void setContext(Context context, CarAlarmServiceConnection connection) {
        setContext(context);
        mConnection = connection;
        mRepository = new Repository(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.settings_fragment,
				container, false);
		/*TextView dummyTextView = (TextView) rootView
				.findViewById(R.id.section_label);
		dummyTextView.setText(Integer.toString(getArguments().getInt(
				ARG_SECTION_NUMBER)));*/
        //ContactsAsynkTask oContacts = new ContactsAsynkTask();
        //oContacts.execute();
        mCheckService   = (CheckBox) rootView.findViewById(R.id.chkService);
        mCheckService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("service", ((CheckBox) v).isChecked());
                            editor.apply();
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("service", ((CheckBox) v).isChecked());
                            if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"Click1 start on Boot is: "+prefs.getBoolean("service",false));
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
            }
        });
        mCheckGPS       = (CheckBox) rootView.findViewById(R.id.chkGPS);
        mCheckGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("gps", ((CheckBox) v).isChecked());
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("gps", ((CheckBox) v).isChecked());
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
            }
        });
        mCheckSMS       = (CheckBox) rootView.findViewById(R.id.chkSMS);
        mCheckSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("sms", ((CheckBox) v).isChecked());
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("sms", ((CheckBox) v).isChecked());
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

            }
        });

        mCheckCall       = (CheckBox) rootView.findViewById(R.id.chkCall);
        mCheckCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("call", ((CheckBox) v).isChecked());
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("call", ((CheckBox) v).isChecked());
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

            }
        });

        mCheckSMSRemoteControl       = (CheckBox) rootView.findViewById(R.id.chkSMSRemoteControl);
        mCheckSMSRemoteControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("smsremotecontrol", ((CheckBox) v).isChecked());
                        editor.commit();
                        editor.apply();
                        mRepository.putPref("smsremotecontrol", ((CheckBox) v).isChecked());
                        if(mConnection!=null && mConnection.mIService!=null){
                            try {
                                mConnection.mIService.reloadConfig();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }
        });

        mCheckAcc       = (CheckBox) rootView.findViewById(R.id.chkAcc);
        mCheckAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("accererometer", ((CheckBox) v).isChecked());
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("accererometer", ((CheckBox) v).isChecked());
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
            }
        });

        mCheckBluetooth = (CheckBox) rootView.findViewById(R.id.chkBluetooth);
        mCheckBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Looper.getMainLooper();
                                Looper.prepareMainLooper();
                            }catch (IllegalStateException e){
                                if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(),"Looper prepare not neded");
                            }
                            if (((CheckBox) v).isChecked() && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                                 Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                                 enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                                 enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                 mContext.startActivity(enableBtIntent);
                                 //BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE
                            }
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("bluetooth", ((CheckBox) v).isChecked());
                            editor.commit();
                            editor.apply();
                            mRepository.putPref("bluetooth", ((CheckBox) v).isChecked());
                            if(mConnection!=null && mConnection.mIService!=null){
                                try {
                                    mConnection.mIService.reloadConfig();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
            }
        });

        mBtnPhoneDetails   =   (ImageButton) rootView.findViewById(R.id.btnPhone);
        mBtnPhoneDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneDetails("phone");
            }
        });

        mBtnPhoneDetailsCall   =   (ImageButton) rootView.findViewById(R.id.btnCallPhone);
        mBtnPhoneDetailsCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneDetails("callphone");
            }
        });

        mBtnSMSPhoneForRemoteControl   =   (ImageButton) rootView.findViewById(R.id.btnSMSRemoteControl);
        mBtnSMSPhoneForRemoteControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhoneDetails("smsremote");
            }
        });

        mStartStopService =     (ToggleButton) rootView.findViewById(R.id.tglBtnStartStop);

        mStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!((ToggleButton) v).isChecked()){
                        if(mConnection!=null && mConnection.mIService!=null){
                            if(Const.IS_DEBUG) Log.d(this.getClass().getCanonicalName(),"shutdown servces OK");
                            mConnection.mIService.shutDown();
                        }else{
                            if(Const.IS_DEBUG) Log.d(this.getClass().getCanonicalName(),"shutdown servces KO service is null");
                        }
                    }else{
                        Intent serviceIntent = new Intent(mContext,CarAlarmGuard.class);
                        serviceIntent.putExtra("forceStart",true);
                        mContext.startService(serviceIntent);
                    }
                }catch (RemoteException e) {
                    Log.e(this.getClass().getCanonicalName(),"Remote Exception on connect to Service");
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.e(this.getClass().getCanonicalName(),"null to Service");
                    e.printStackTrace();
                }
            }
        });
        if(mConnection!=null && mConnection.mIService!=null){
            mStartStopService.setChecked(true);
        }else{
            mStartStopService.setChecked(false);
        }


		return rootView;
	}
	public void setContext(Context context){
        mContext=context;
        prefs = mContext.getSharedPreferences(Const.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        if(mConnection!=null){
            mStartStopService.setChecked(true);
        }else{
            mStartStopService.setChecked(false);
        }


        //checkSensor();



        if(prefs!=null){
            if(prefs.getBoolean("bluetooth",false)) mCheckBluetooth.setChecked(true);
            else mCheckBluetooth.setChecked(false);

            if(prefs.getBoolean("accererometer",false)) mCheckAcc.setChecked(true);
            else mCheckAcc.setChecked(false);

            if(prefs.getBoolean("gps",false)) mCheckGPS.setChecked(true);
            else mCheckGPS.setChecked(false);

            if(prefs.getBoolean("sms",false)) mCheckSMS.setChecked(true);
            else mCheckSMS.setChecked(false);

            if(prefs.getBoolean("call",false)) mCheckCall.setChecked(true);
            else mCheckCall.setChecked(false);

            if(prefs.getBoolean("smsremotecontrol",false)) mCheckSMSRemoteControl.setChecked(true);
            else mCheckSMSRemoteControl.setChecked(false);

            if(prefs.getBoolean("service",false)) mCheckService.setChecked(true);
            else mCheckService.setChecked(false);
        }else{
            mCheckBluetooth.setChecked(false);
            mCheckAcc.setChecked(false);
            mCheckGPS.setChecked(false);
            mCheckSMS.setChecked(false);
            mCheckCall.setChecked(false);
            mCheckService.setChecked(false);
        }
        super.onResume();
    }

    private void checkSensor() {
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            mCheckAcc.setEnabled(false);
            mCheckAcc.setChecked(false);
        }

    }
    /**
     * Seleziona l'orientamento della pagina
     *
     *
     *
     * @param callphone*/
    private void showPhoneDetails(final String callphone){
        AlertDialog.Builder builder;
        final AlertDialog alertDialog;


        if(callphone.equals("phone")){
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final LinearLayout dialoglayout = (LinearLayout) inflater.inflate(R.layout.phone_settings,(ViewGroup) getActivity().findViewById(R.id.mainlayout));
            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
            EditText mEditPhone2 = (EditText) dialoglayout.findViewById(R.id.editPhone2);
            EditText mEditSMS    = (EditText) dialoglayout.findViewById(R.id.editSMS);
            mEditPhone1.setText(prefs.getString(callphone+"1",""));
            mEditPhone2.setText(prefs.getString(callphone+"2",""));
            mEditSMS.setText(prefs.getString("smsText",""));

            builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(true).setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
                            EditText mEditPhone2 = (EditText) dialoglayout.findViewById(R.id.editPhone2);
                            EditText mEditSMS    = (EditText) dialoglayout.findViewById(R.id.editSMS);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(callphone+"1", mEditPhone1.getText().toString());
                            editor.putString(callphone+"2", mEditPhone2.getText().toString());
                            editor.putString("smsText", mEditSMS.getText().toString());
                            editor.commit();
                        }
                    }).start();
                }
            }).setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


            try{
                builder.setTitle(this.getString(R.string.app_name));
                builder.setView(dialoglayout);
                alertDialog = builder.create();
                alertDialog.show();
            }catch (android.view.WindowManager.BadTokenException e){
                Log.e(this.getClass().getCanonicalName(),"Error open dialog!");
            }


        }else if (callphone.equals("callphone")){
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final LinearLayout dialoglayout = (LinearLayout) inflater.inflate(R.layout.call_phone_settings,(ViewGroup) getActivity().findViewById(R.id.mainlayout));
            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
            mEditPhone1.setText(prefs.getString(callphone+"1",""));
            builder = new AlertDialog.Builder(mContext);

            builder.setCancelable(true).setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(callphone+"1", mEditPhone1.getText().toString());
                            editor.commit();
                        }
                    }).start();

                }
            }).setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            try{
                builder.setTitle(this.getString(R.string.app_name));
                builder.setView(dialoglayout);
                alertDialog = builder.create();
                alertDialog.show();
            }catch (android.view.WindowManager.BadTokenException e){
                Log.e(this.getClass().getCanonicalName(),"Error open dialog!");
            }
        }else{
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final LinearLayout dialoglayout = (LinearLayout) inflater.inflate(R.layout.call_phone_settings,(ViewGroup) getActivity().findViewById(R.id.mainlayout));
            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
            mEditPhone1.setText(prefs.getString(callphone+"1",""));
            builder = new AlertDialog.Builder(mContext);

            builder.setCancelable(true).setPositiveButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            EditText mEditPhone1 = (EditText) dialoglayout.findViewById(R.id.editPhone1);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(callphone+"1", mEditPhone1.getText().toString());
                            editor.commit();
                        }
                    }).start();

                }
            }).setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            try{
                builder.setTitle(this.getString(R.string.app_name));
                builder.setView(dialoglayout);
                alertDialog = builder.create();
                alertDialog.show();
            }catch (android.view.WindowManager.BadTokenException e){
                Log.e(this.getClass().getCanonicalName(),"Error open dialog!");
            }
        }

    }
	/*public void onActivityResult(int reqCode, int resultCode, Intent data){ 
		super.onActivityResult(reqCode, resultCode, data);

		switch(reqCode)
		{
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK)
			{
				Uri contactData = data.getData();
				CursorLoader c = new CursorLoader(getActivity(),contactData, null, null, null, null);

				if (c.moveToFirst())
				{
					String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone =
							c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) 
					{
						Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
						phones.moveToFirst();
						String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						// Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();
						setCn(cNumber);
					}
				}
			}
		}*/

    /********************************************
     * TASK ASINCRONI PER LE VARIE OPERAZIONI 	*
     * 											*
     * 											*
     * ******************************************/

     class ContactsAsynkTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Cursor cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null,null, null,null);
            while (cursor.moveToNext()) {
                Log.v(this.getClass().getCanonicalName(),"NAME: "+ cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                Log.v(this.getClass().getCanonicalName(), "ID: " + cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = mContext.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)),null, null);
                    while (pCur.moveToNext()) {
                        Log.v(this.getClass().getCanonicalName(), "PHONE: " + pCur.getString(pCur.getColumnIndex("DATA1")));
                    }
                    pCur.close();
                }
            }

            return true;
        }
    }


}