package security.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import security.car.alarm.R;
import security.utils.CarAlarmServiceConnection;


/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class AboutSectionFragment extends Fragment {
    private final static String APP_PNAME = "com.glm.caralarm";
    private Context mContext;
    private CarAlarmServiceConnection mConnection;



	public void setContext(Context context,CarAlarmServiceConnection connection) {
        setContext(context);
        mConnection=connection;
	}
    public void setContext(Context context){
        mContext=context;
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.about_fragment,
				container, false);

		return rootView;
	}
}