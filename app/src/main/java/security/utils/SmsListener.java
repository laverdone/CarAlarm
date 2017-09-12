package security.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.util.logging.*;

import security.car.alarm.R;
import security.service.CarAlarmGuard;

public class SmsListener extends BroadcastReceiver {

    private Context mContext;
    private SharedPreferences preferences;
    private String mSmsPhoneForRemoteControl;
    private boolean isSMSRemoteControl=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;

        preferences = context.getSharedPreferences(Const.SHARED_PREFS, Context.MODE_PRIVATE);
        mSmsPhoneForRemoteControl = preferences.getString("smsremote1","");

        isSMSRemoteControl=preferences.getBoolean("smsremotecontrol",false);

        if(!isSMSRemoteControl) return;

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){

                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        if(Const.IS_DEBUG) {
                            Log.d(this.getClass().getCanonicalName(),"sms receive from: "+msg_from);
                            Logger.log("sms receive from: "+msg_from);
                        }
                        if(msg_from.contains(mSmsPhoneForRemoteControl)){
                            String msgBody = msgs[i].getMessageBody();
                            if(Const.IS_DEBUG) {
                                Log.d(this.getClass().getCanonicalName(),"valid remote sms receive from: "+mSmsPhoneForRemoteControl+" command is: "+msgBody);
                                Logger.log("valid remote sms receive from: "+mSmsPhoneForRemoteControl+" command is: "+msgBody);
                            }

                            //TODO
                            if(msgBody.equals("activate")){
                                sendCommandToService("activate");
                            }else if (msgBody.equals("deactivate")){
                                sendCommandToService("deactivate");
                            }else if(msgBody.equals("status")){
                                sendCommandToService("status");
                            }
                        }

                    }
                }catch(Exception e){
                    Log.e("SMS Exception caught",e.getMessage());
                }
            }
        }
    }

    private void sendCommandToService(String command) {

                if(command.equals("activate")){
                    Intent serviceIntent = new Intent(mContext,CarAlarmGuard.class);
                    serviceIntent.putExtra("forceStart",true);
                    serviceIntent.putExtra("activate",true);
                    mContext.startService(serviceIntent);
                }else if (command.equals("deactivate")){
                    Intent serviceIntent = new Intent(mContext,CarAlarmGuard.class);
                    serviceIntent.putExtra("forceStart",true);
                    serviceIntent.putExtra("activate",false);
                    mContext.startService(serviceIntent);
                }else if(command.equals("status")){
                    SmsManager smsManager = SmsManager.getDefault();

                }

    }
}