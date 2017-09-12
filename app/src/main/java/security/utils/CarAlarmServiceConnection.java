package security.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import security.service.CarAlarmGuard;
import security.service.ICarAlarmGuard;

/**
 * Classe Connection che stabilisce il bind col servizio
 *
 * @author gianluca masci aka (GLM)
 *
 * **/
public class CarAlarmServiceConnection implements ServiceConnection
{
    public boolean mIsBound=false;
    public ICarAlarmGuard mIService;
    public Context mContext;

    public CarAlarmServiceConnection(Context context){
        mContext=context;
        doBindService();
    }
    /**
     * Disconnetto dal servizio
     * */
    public void destroy(){
        doUnbindService();
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        try{
            mIService= ICarAlarmGuard.Stub.asInterface(service);
            Log.i(this.getClass().getCanonicalName(), "Connect to Service");

        }catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "onServiceConnected->Remote Exception"+e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        Log.i(this.getClass().getCanonicalName(), "Disconnect to Service");
	               /* Toast.makeText(StopwatchActivity.this, "TrainerServiceConnection->onServiceDisconnected"+R.string.pause_exercise,
	    	                Toast.LENGTH_LONG).show();*/
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        //Intent bindIntent = new Intent(Main.this, MessengerService.class);
        //bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        if(mContext!=null){
            Intent serviceIntent = new Intent(mContext,CarAlarmGuard.class);
            try {
                mIsBound = mContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
                Log.i(this.getClass().getCanonicalName(), "doBindService Binding from Services OK");
            }catch (RuntimeException e){
                mIsBound = mContext.getApplicationContext().bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
                Log.i(this.getClass().getCanonicalName(), "doBindService Binding from smsReceive Services OK");
            }
        }
        Log.i(this.getClass().getCanonicalName(), "Binding from Services");
    }
    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with

            Log.i(this.getClass().getCanonicalName(), "UnBinding from Services");

            // Detach our existing connection.
            mContext.unbindService(this);
            mIsBound = false;
        }
    }
}