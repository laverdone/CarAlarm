package security.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import security.car.alarm.R;
import security.service.CarAlarmGuard;
import security.utils.CarAlarmServiceConnection;
import security.utils.Const;
import security.utils.db.Repository;


public class StatusSectionFragment extends Fragment {
	//private Button oBtnRed;
	//private Button oBtnYellow;
	//private Button oBtnGreen;

    private ImageButton mChangeStatus=null;
    private Context mContext;
    public boolean mCheckStatus=true;
    private CarAlarmServiceConnection mConnection = null;
    private StatusService mStatus;
    private FloatingActionButton mPairButton;
    private SharedPreferences prefs;
    private Repository mRepository=null;
    /**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";



	public void setContext(Context context, CarAlarmServiceConnection connection) {
        setContext(context);
        mConnection = connection;
	}

    public void setContext(Context context){
        mContext=context;
    }

    @Override
    public void onResume() {
        if(mConnection!=null && mConnection.mIService!=null){
            try {
                mChangeStatus.setVisibility(View.VISIBLE);

                if(mConnection.mIService.getStatus()==Const.STATUS_STOP){
                    mChangeStatus.setImageResource(R.drawable.deactivated);
                }else if(mConnection.mIService.getStatus()==Const.STATUS_RUNNING){
                    mChangeStatus.setImageResource(R.drawable.activated);
                }
                /*if(mConnection.mIService.getStatus()==Const.STATUS_RUNNING){
                    //Activate
                    oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                    oBtnGreen.setBackgroundResource(R.drawable.circle_green);
                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                }else if(mConnection.mIService.getStatus()==Const.STATUS_STOP){
                    //DeActivate
                    oBtnRed.setBackgroundResource(R.drawable.circle_red);
                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                }else if(mConnection.mIService.getStatus()==Const.STATUS_TESTING){
                    //test
                    oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                    oBtnYellow.setBackgroundResource(R.drawable.circle_yellow);
                }else{
                    oBtnRed.setBackgroundResource(R.drawable.circle_red);
                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                }*/
                mCheckStatus=true;
                mStatus = new StatusService();
                mStatus.execute();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
           /* oBtnRed.setBackgroundResource(R.drawable.circle_red);
            oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
            oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);*/
            mChangeStatus.setVisibility(View.GONE);
        }


        super.onResume();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.status_content,
				container, false);
        mChangeStatus = (ImageButton) rootView.findViewById(R.id.btnChangeStatus);


        mChangeStatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int iDuration=300;
                if(mConnection!=null && mConnection.mIService!=null){

                    try {
                        if(mConnection.mIService.getStatus()==Const.STATUS_RUNNING){
                            mConnection.mIService.setAlarmOff();
                            ((ImageButton) v).setBackgroundResource(R.drawable.deactivated);
                        }else if (mConnection.mIService.getStatus()==Const.STATUS_STOP){
                            mConnection.mIService.setAlarmOn();
                            ((ImageButton) v).setBackgroundResource(R.drawable.activated);
                        }

                    } catch (RemoteException e) {
                        Log.e(this.getClass().getCanonicalName(), "Error Activate Alarm!");
                        e.printStackTrace();
                        iDuration=500;
                    }
                }
            }
        });

        mPairButton = (FloatingActionButton) rootView.findViewById(R.id.pairButton);
        mPairButton.setVisibility(View.VISIBLE);
        mPairButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(enableBtIntent);
                if(mConnection!=null && mConnection.mIService!=null){
                    try {
                        mConnection.mIService.reloadConfig();
                        Snackbar.make(v, "Car Alarm now Visible", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else{
                    mRepository = new Repository(mContext);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("bluetooth", true);
                    editor.commit();
                    editor.apply();
                    try{
                        mRepository.putPref("bluetooth", true);
                        //Start Service
                        Intent serviceIntent = new Intent(mContext,CarAlarmGuard.class);
                        mContext.startService(serviceIntent);
                    }catch (IllegalStateException e){
                        if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"IllegalStateException Car Alarm Service Start not reload config");
                    }catch (NullPointerException e1){
                        if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"NullPointerException not reload config");
                    }
                }
            }
        });
        /*oBtnRed 	= (Button) rootView.findViewById(R.id.btnRed);
		oBtnYellow 	= (Button) rootView.findViewById(R.id.btnYellow);
		oBtnGreen 	= (Button) rootView.findViewById(R.id.btnGreen);
		
		oBtnRed.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int iDuration=300;

					if(mConnection!=null && mConnection.mIService!=null){

                        try {
                            if(mConnection.mIService.getStatus()!=Const.STATUS_STOP){
                                mConnection.mIService.setAlarmOff();
                                ((Button) v).setBackgroundResource(R.drawable.circle_red);
                                oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                                oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                            }

                        } catch (RemoteException e) {
                            Log.e(this.getClass().getCanonicalName(), "Error Activate Alarm!");
                            e.printStackTrace();
                            iDuration=500;
                        }
                    }
                    Vibrator vb = (Vibrator) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().VIBRATOR_SERVICE);

					// Vibrate for 300 milliseconds
					//vb.vibrate(iDuration);

			}
		});
		
		oBtnYellow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(((Button) v).getBackground()!=getResources().getDrawable(R.drawable.circle_yellow)){
                    int iDuration=300;

                    if(mConnection!=null && mConnection.mIService!=null){

                        try {
                            if(mConnection.mIService.getStatus()!=Const.STATUS_TESTING){
                                ((Button) v).setBackgroundResource(R.drawable.circle_yellow);
                                oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                                oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                                mConnection.mIService.setAlarmTest();
                            }

                        } catch (RemoteException e) {
                            Log.e(this.getClass().getCanonicalName(), "Error test Alarm!");
                            e.printStackTrace();
                            iDuration=500;
                        }
                    }
                    Vibrator vb = (Vibrator) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().VIBRATOR_SERVICE);

					// Vibrate for 300 milliseconds
					//vb.vibrate(iDuration);

				}
			}
		});

		oBtnGreen.setOnClickListener(new OnClickListener() {
	
		@Override
		public void onClick(View v) {
				
				    int iDuration=300;

                    if(mConnection!=null && mConnection.mIService!=null){
                        try {
                            if(mConnection.mIService.getStatus()!=Const.STATUS_RUNNING){
                                mConnection.mIService.setAlarmOn();
                                ((Button) v).setBackgroundResource(R.drawable.circle_green);
                                oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                                oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                            }

                        } catch (RemoteException e) {
                            Log.e(this.getClass().getCanonicalName(), "Error deActivate Alarm!");
                            e.printStackTrace();
                            iDuration=500;
                        }
                    }
                    Vibrator vb = (Vibrator) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().VIBRATOR_SERVICE);

					// Vibrate for 300 milliseconds
					//vb.vibrate(iDuration);

				}
		});

        if(mConnection==null){
            oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
            oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
            oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
            oBtnRed.setEnabled(false);
            oBtnGreen.setEnabled(false);
            oBtnYellow.setEnabled(false);
        }*/
		/*TextView dummyTextView = (TextView) rootView
				.findViewById(R.id.section_label);
		dummyTextView.setText(Integer.toString(getArguments().getInt(
				ARG_SECTION_NUMBER)));*/
        mStatus = new StatusService();
        mStatus.execute();
		return rootView;
	}

    @Override
    public void onDestroyView() {
        mCheckStatus=false;
        mStatus.cancel(true);
        mStatus=null;
        super.onDestroyView();
    }

    class StatusService extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            while(mCheckStatus){
                if(mConnection!=null &&
                        mConnection.mIService!=null){
                    try {

                        Handler mHandler = new Handler(Looper.getMainLooper());
                        if(mConnection.mIService.getStatus()==Const.STATUS_STOP){
                            mHandler.post(new Runnable() {
                                public void run() {
                                   mChangeStatus.setVisibility(View.VISIBLE);
                                    mPairButton.setVisibility(View.VISIBLE);
                                   mChangeStatus.setImageResource(R.drawable.deactivated);

                                }
                            });
                        }else if(mConnection.mIService.getStatus()==Const.STATUS_RUNNING){
                            mHandler.post(new Runnable() {
                                public void run() {
                                    mChangeStatus.setVisibility(View.VISIBLE);
                                    mPairButton.setVisibility(View.VISIBLE);
                                    mChangeStatus.setImageResource(R.drawable.activated);

                                }
                            });
                        }
                        /*if(mConnection.mIService.getStatus()==Const.STATUS_STOP){
                            mHandler.post(new Runnable() {
                                public void run() {
                                    oBtnRed.setBackgroundResource(R.drawable.circle_red);
                                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);

                                }
                            });
                        }else if(mConnection.mIService.getStatus()==Const.STATUS_RUNNING){
                            mHandler.post(new Runnable() {
                                public void run() {
                                    oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnGreen.setBackgroundResource(R.drawable.circle_green);
                                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);

                                }
                            });
                        }else{
                            mHandler.post(new Runnable() {
                                public void run() {
                                    oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnYellow.setBackgroundResource(R.drawable.circle_yellow);

                                }
                            });
                        }
                        if(mConnection==null){
                            mHandler.post(new Runnable() {
                                public void run() {
                                    oBtnRed.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnGreen.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnYellow.setBackgroundResource(R.drawable.circle_disabled);
                                    oBtnRed.setEnabled(false);
                                    oBtnGreen.setEnabled(false);
                                    oBtnYellow.setEnabled(false);

                                }
                            });
                        }*/
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mCheckStatus=false;
                        break;
                    }
                    /*try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/

                }
            }
            return true;
        }
    }
}