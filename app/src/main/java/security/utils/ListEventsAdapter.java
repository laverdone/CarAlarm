package security.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import security.bean.GuardLog;
import security.car.alarm.R;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gianluca on 12/09/13.
 */
public class ListEventsAdapter extends BaseAdapter{

    private ArrayList<GuardLog> mEvets;
    private Context mContext;
    public ListEventsAdapter(Context context, ArrayList<GuardLog> evets){
        mContext=context;
        mEvets=evets;
    }
    @Override
    public int getCount() {
        return mEvets.size();
    }

    @Override
    public Object getItem(int position) {

        return mEvets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mEvets.get(position).logID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GuardLog oEvent = getEvent(position);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  infalInflater.inflate(R.layout.event_item, null);
        }
        TextView mDateEvent = (TextView) convertView.findViewById(R.id.textDateEvent);
        SimpleDateFormat iso8601Format = new SimpleDateFormat(
                "dd MMMM yyyy",Locale.getDefault());

        mDateEvent.setText(iso8601Format.format(oEvent.logDATE));

        TextView mEvent = (TextView) convertView.findViewById(R.id.textEvent);
        mEvent.setText(oEvent.logDESC);
        ImageView mEventLevel1 = (ImageView) convertView.findViewById(R.id.imgLevel1);
        ImageView mEventLevel2 = (ImageView) convertView.findViewById(R.id.imgLevel2);
        ImageView mEventLevel3 = (ImageView) convertView.findViewById(R.id.imgLevel3);

        if(oEvent.logTYPE.compareToIgnoreCase("ALERT")==0){
            mEventLevel1.setImageResource(android.R.drawable.button_onoff_indicator_on);
            mEventLevel2.setImageResource(android.R.drawable.button_onoff_indicator_on);
            mEventLevel3.setImageResource(android.R.drawable.button_onoff_indicator_on);
        }else if(oEvent.logTYPE.compareToIgnoreCase("WARN")==0){
            mEventLevel1.setImageResource(android.R.drawable.button_onoff_indicator_off);
            mEventLevel2.setImageResource(android.R.drawable.button_onoff_indicator_on);
            mEventLevel3.setImageResource(android.R.drawable.button_onoff_indicator_on);
        }else{
            mEventLevel1.setImageResource(android.R.drawable.button_onoff_indicator_off);
            mEventLevel2.setImageResource(android.R.drawable.button_onoff_indicator_off);
            mEventLevel3.setImageResource(android.R.drawable.button_onoff_indicator_on);
        }


        return convertView;
    }

    private GuardLog getEvent(int position) {
        return mEvets.get(position);
    }
}
