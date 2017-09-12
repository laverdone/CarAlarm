package security.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import security.bean.GuardLog;

import security.car.alarm.MainActivity;
import security.car.alarm.R;
import security.utils.CarAlarmServiceConnection;
import security.utils.ListEventsAdapter;
import security.utils.db.Repository;

import java.util.ArrayList;

/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class EventsSectionFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";
    private ListView mListEvents;
    private ListEventsAdapter mListEventsAdapter;
    private Context mContext;
    private ArrayList<GuardLog> mEvets;
    private String mFilterLevel=null;
    private ProgressDialog oWaitForSave=null;
    private CarAlarmServiceConnection mConnection = null;

	public void setContext(Context context, CarAlarmServiceConnection connection) {
        setContext(context);
        mConnection = connection;
	}

    public void setContext(Context context){
        mContext=context;
        EventsAsynkTask oListEvetsTask = new EventsAsynkTask(mFilterLevel);
        oListEvetsTask.execute();
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.events_fragment,
				container, false);

		/*TextView dummyTextView = (TextView) rootView
				.findViewById(R.id.section_label);
		dummyTextView.setText(Integer.toString(getArguments().getInt(
				ARG_SECTION_NUMBER)));*/
        mListEvents = (ListView) rootView.findViewById(R.id.listEvents);
        LinearLayout mBtnFilterLevel1 = (LinearLayout) rootView.findViewById(R.id.btnLevel1);
        LinearLayout mBtnFilterLevel2 = (LinearLayout) rootView.findViewById(R.id.btnLevel2);
        LinearLayout mBtnFilterLevel3 = (LinearLayout) rootView.findViewById(R.id.btnLevel3);
        ImageButton mBtnEmptyLog = (ImageButton) rootView.findViewById(R.id.imgBtnDeleteAll);
        mBtnEmptyLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mContext);

                // set title
                alertDialogBuilder.setTitle(getString(R.string.app_name));

                // set dialog message
                alertDialogBuilder
                        .setMessage(getString(R.string.deletelog))
                        .setCancelable(false)
                        .setPositiveButton(getString(android.R.string.yes),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int id) {
                                if(isAdded()){
                                    oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Repository mRepository = new Repository(mContext);
                                        mRepository.deleteGuardLog(mFilterLevel);
                                        mEvets=mRepository.loadGuardLog(mFilterLevel);

                                        mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
                                        mListEventsAdapter.notifyDataSetChanged();
                                        mListEventsAdapter.notifyDataSetInvalidated();
                                        Handler mHandler = new Handler(Looper.getMainLooper());
                                        mHandler.post(new Runnable() {
                                            public void run() {

                                                mListEvents.setAdapter(mListEventsAdapter);
                                                try{
                                                    if(oWaitForSave!=null) oWaitForSave.dismiss();
                                                }catch (IllegalArgumentException e){
                                                    Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
                                                }
                                            }
                                        });
                                    }
                                }).start();
                             }
                        })
                        .setNegativeButton(getString(android.R.string.no),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.dismiss();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });


        mBtnFilterLevel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdded()){
                    oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
                }
                Log.v(this.getClass().getCanonicalName(),"Filter Level 1");
                mFilterLevel="INFO";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Repository mRepository = new Repository(mContext);
                        mEvets=mRepository.loadGuardLog(mFilterLevel);
                        mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
                        mListEventsAdapter.notifyDataSetChanged();
                        mListEventsAdapter.notifyDataSetInvalidated();
                        Handler mHandler = new Handler(Looper.getMainLooper());

                        mHandler.post(new Runnable() {
                            public void run() {

                                mListEvents.setAdapter(mListEventsAdapter);
                                try{
                                    if(oWaitForSave!=null) oWaitForSave.dismiss();
                                }catch (IllegalArgumentException e){
                                    Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
                                }
                            }
                        });
                    }
                }).start();
            }
        });
        mBtnFilterLevel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdded()){
                    oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
                }
                Log.v(this.getClass().getCanonicalName(),"Filter Level 2");
                mFilterLevel="WARN";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Repository mRepository = new Repository(mContext);
                        mEvets=mRepository.loadGuardLog(mFilterLevel);
                        mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
                        mListEventsAdapter.notifyDataSetChanged();
                        mListEventsAdapter.notifyDataSetInvalidated();
                        Handler mHandler = new Handler(Looper.getMainLooper());

                        mHandler.post(new Runnable() {
                            public void run() {

                                mListEvents.setAdapter(mListEventsAdapter);
                                try{
                                    if(oWaitForSave!=null) oWaitForSave.dismiss();
                                }catch (IllegalArgumentException e){
                                    Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
                                }
                            }
                        });
                    }
                }).start();

            }
        });
        mBtnFilterLevel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAdded()){
                    oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
                }
                Log.v(this.getClass().getCanonicalName(),"Filter Level 3");
                mFilterLevel="ALERT";

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Repository mRepository = new Repository(mContext);
                        mEvets=mRepository.loadGuardLog(mFilterLevel);
                        mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
                        mListEventsAdapter.notifyDataSetChanged();
                        mListEventsAdapter.notifyDataSetInvalidated();
                        Handler mHandler = new Handler(Looper.getMainLooper());

                        mHandler.post(new Runnable() {
                            public void run() {

                                mListEvents.setAdapter(mListEventsAdapter);
                                try{
                                    if(oWaitForSave!=null) oWaitForSave.dismiss();
                                }catch (IllegalArgumentException e){
                                    Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        return rootView;
	}


    /********************************************
     * TASK ASINCRONI PER LE VARIE OPERAZIONI 	*
     * 											*
     * 											*
     * ******************************************/

    class EventsAsynkTask extends AsyncTask {


        private String mFilter;
        public EventsAsynkTask(String filter){
            mFilter=filter;
        }
        @Override
        protected Object doInBackground(Object[] params) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            Repository mRepository = new Repository(mContext);
            mEvets=mRepository.loadGuardLog(mFilter);

            return false;
        }

        @Override
        protected void onPostExecute(Object aBoolean) {
            mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
            mListEventsAdapter.notifyDataSetChanged();
            mListEventsAdapter.notifyDataSetInvalidated();
            mListEvents.setAdapter(mListEventsAdapter);
            try{
                if(oWaitForSave!=null) oWaitForSave.dismiss();
            }catch (IllegalArgumentException e){
                Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
            }
            this.cancel(true);
            super.onPostExecute(aBoolean);
        }
        @Override
        protected void onPreExecute() {
            if(isAdded()){
                oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
            }
            super.onPreExecute();
        }
    }

    /********************************************
     * TASK ASINCRONI PER LE VARIE OPERAZIONI 	*
     * 											*
     * 											*
     * ******************************************/

    class DeleteEventsAsynkTask extends AsyncTask {
        private ProgressDialog oWaitForSave=null;

        private String mFilter;
        public DeleteEventsAsynkTask(String filter){
            mFilter=filter;
        }

        @Override
        protected Boolean doInBackground(Object[] params) {
            if(oWaitForSave!=null){
                oWaitForSave.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        oWaitForSave.dismiss();
                        Log.v(this.getClass().getCanonicalName(),"Cancel Save");
                    }
                });
            }
            Repository mRepository = new Repository(mContext);
            mRepository.deleteGuardLog(mFilter);
            mEvets=mRepository.loadGuardLog(mFilter);

            return true;
        }

        @Override
        protected void onPostExecute(Object aBoolean) {
            mListEventsAdapter=new ListEventsAdapter(mContext,mEvets);
            mListEventsAdapter.notifyDataSetChanged();
            mListEventsAdapter.notifyDataSetInvalidated();
            mListEvents.setAdapter(mListEventsAdapter);
            try{
                if(oWaitForSave!=null) oWaitForSave.dismiss();
            }catch (IllegalArgumentException e){
                Log.e(this.getClass().getCanonicalName(),"error dismiss wait");
            }
            super.onPostExecute(aBoolean);
        }
        @Override
        protected void onPreExecute() {
            if(isAdded()){
                oWaitForSave = ProgressDialog.show(getActivity(),getString(R.string.app_name),getString(R.string.wait),true,true,null);
            }
            super.onPreExecute();
        }
    }
}