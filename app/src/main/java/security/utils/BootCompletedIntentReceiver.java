package security.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;

import security.service.CarAlarmGuard;
import security.utils.db.Repository;

/**
 * Created by gianluca on 10/09/13.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    private Repository mRepository = null;
    private HashMap<String, Boolean> mPrefs = null;
    private boolean isService=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            mRepository = new Repository(context);
            try{
                mPrefs = mRepository.getAllPref();
                isService       =   mPrefs.get("service");
            }catch (IllegalStateException e){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"IllegalStateException Car Alarm Service Start not reload config");
            }catch (NullPointerException e1){
                if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(),"NullPointerException not reload config");
            }
            if(isService) {
                Intent serviceIntent = new Intent(context, CarAlarmGuard.class);
                serviceIntent.putExtra("forceStart", true);
                context.startService(serviceIntent);
            }
        }
    }
}
