package security.utils.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import security.service.CarAlarmGuard;
import security.utils.Base64Coder;
import security.utils.Const;
import security.utils.db.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BlueToothHelper {
    private double lastHeartRate = 0;
    private double heartRate = 0;
    private BluetoothDevice oDevice;
    private boolean bConnect=false;
    private static final UUID CAR_ALARM_UUID =
            UUID.fromString("4806BBF1-E4F0-4FBA-98CB-B70C4D8DD7E5");
    private BluetoothServerSocket mBluetoothServerSocket;
    private BluetoothAdapter mBluetoothAdapter;

    private AcceptThread mAcceptThread;;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public int mState;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_ERROR = -1;       // we're doing error
    private CarAlarmGuard mCarAlarmService;
    private Context mContext;
    private Repository mRepository=null;
    /**
     * Costruttore che inizializza gli oggetti BlueTooth
     *
     * */
    public BlueToothHelper(Context context, CarAlarmGuard service){
        mContext=context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mRepository = new Repository(mContext);
       /*     if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.getApplicationContext().startActivity(enableBtIntent);
            }else{

                if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"Socket created!");
            }*/
        mCarAlarmService=service;
        setState(STATE_NONE);
    }



    /**
     * Ritorna se il dispositivo supporta il bluetooth
     *
     *
     * */
    public boolean isBluetoothAvail(){

        if (mBluetoothAdapter == null) {
            //Log.v(this.getClass().getCanonicalName(),"BlueTooth not supported");
            return false;
        }
        return true;
    }
    /**
     * Risorna lo stato del BlueTooth
     *
     * */
    public boolean isBlueToothEnabled(){
        if(mBluetoothAdapter!=null){
            if (!mBluetoothAdapter.isEnabled()) {
                //Log.v(this.getClass().getCanonicalName(),"BlueTooth not enabled");
			    /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);*/
                return false;
            }
            return true;
        }
        return false;
    }
    /**
     * Cerca per dispositivi associati al CELL.
     *
     *
     * *//*
    public void searchPairedDevice(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.v("oBTHelper","pairedDevices: "+pairedDevices.size());
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if(device!=null){
                    if(device.getBondState()!=BluetoothDevice.BOND_NONE){
                        CarAlarm oCarAlarm = new CarAlarm();
                        oCarAlarm.name = device.getName();
                        oCarAlarm.address =device.getAddress();
                        oCarAlarm.device = device;
                        aCardioDevice.add(oCarAlarm);
                        oCarAlarm=null;
                    }
                }

            }
        }
    }
    *//**Ritorna i nomi dispositivi associati*//*
    public synchronized ArrayList<CarAlarm> getaCarAlarm() {
        return aCardioDevice;
    }
*/

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void waitForConnect() {
        BluetoothServerSocket tmp = null;
        if(mBluetoothServerSocket==null){
            try {
                if(mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                                "CarAlarmSystem", CAR_ALARM_UUID);
                    if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord ");
                } else {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("CarAlarmSystem",CAR_ALARM_UUID);
                    if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," mBluetoothAdapter.listenUsingRfcommWithServiceRecord ");
                }

            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), "waitForConnect IOException Bluetooth");
                e.printStackTrace();
                mBluetoothAdapter.disable();
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            }
            mBluetoothServerSocket = tmp;
        }

        if(mBluetoothServerSocket==null) return;
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," AcceptThread->Start");
        // Start the thread to listen on a BluetoothServerSocket
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();

        setState(STATE_LISTEN);
    }


    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }


    public synchronized int getHeartRate(){
        if(heartRate==0) return (int) lastHeartRate;
        else return (int) heartRate;
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        Log.d("oBTHelper", "connected, Socket connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d("oBTHelper", "stop");

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread.interrupt();
            mConnectedThread = null;
        }
    }


    private class AcceptThread extends Thread {

        public void run() {
            if(!mBluetoothAdapter.isEnabled()) return;
            BluetoothSocket socket = null;
            //if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," mState: "+mState+" STATE_CONNECTED: "+STATE_CONNECTED);
            // Keep listening until exception occurs or a socket is returned
            while (mState != STATE_CONNECTED) {
                //if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," AcceptThread->While ");
                try {
                    if(mBluetoothServerSocket!=null) socket = mBluetoothServerSocket.accept();
                    //if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"incoming connection!");
                    /*Handler mHandler = new Handler(Looper.getMainLooper());

                    mHandler.post(new Runnable() {
                        public void run() {

                            Toast.makeText(mContext,"incoming connection!",Toast.LENGTH_SHORT).show();

                        }
                    });*/
                    //mRepository.guardLoggger("INFO","Car Alarm Guard Service incoming bluetooth connection",0,0,0);
                } catch (IOException e) {
                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(),"AcceptThread Error IO connection!");
                    this.cancel();
                    //e.printStackTrace();
                    setState(STATE_ERROR);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," AcceptThread->socket!=null ");
                    synchronized (BlueToothHelper.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," AcceptThread->socket!=null->STATE_CONNECTING ");
                                manageConnectedSocket(socket);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName()," AcceptThread->socket!=null->STATE_CONNECTED ");
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    if(Const.IS_DEBUG) Log.e(this.getClass().getCanonicalName(), "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                    if(Const.IS_DEBUG) Log.v(this.getClass().getCanonicalName(),"manage incoming connection!");
                    //try {
                    // Do work to manage the connection (in a separate thread)

                    /*} catch (IOException e) {
                        e.printStackTrace();
                    }*/

                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                if(mBluetoothServerSocket!=null) mBluetoothServerSocket.close();
                mBluetoothServerSocket=null;
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(),"Error close socket");
            }
        }

    }







    /**
     *
     * THREAD DI CONNESSIONE BLUETOOTH
     *
     *
     * **/
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(CAR_ALARM_UUID);

            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), "ConnectThread Failed");
                e.printStackTrace();
                //Reconnect to socket
                //oConnectThread = new ConnectThread( oBluetoothAdapter.getRemoteDevice(device.getAddress()));
                //oConnectThread.start();
            }
            mmSocket = tmp;
        }

        public void run() {

            try {
                Log.d(this.getClass().getCanonicalName(), "oBluetoothAdapter.isDiscovering() "+mBluetoothAdapter.isDiscovering());
                mBluetoothAdapter.cancelDiscovery();
                while(mBluetoothAdapter.isDiscovering()){
                    try {
                        Log.v(this.getClass().getCanonicalName(),"Sleep for end discovery sleeping bt");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.e(this.getClass().getCanonicalName(),"Error sleeping bt");
                    }
                }
                // Cancel discovery because it will slow down the connection
		        /*if(oBluetoothAdapter.isDiscovering()){
		        	oBluetoothAdapter.cancelDiscovery();
		        	//oBluetoothAdapter.startDiscovery();
		        }else{
		        	oBluetoothAdapter.startDiscovery();
		        }*/
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception

                if(mmSocket!=null) mmSocket.connect();
                Log.v(this.getClass().getCanonicalName(),"Connect to Cardio Succes!");
            } catch (IOException connectException) {
                try {
                    Log.e(this.getClass().getCanonicalName(), "ConnectThread->thread->run Failed");
                    Log.e(this.getClass().getCanonicalName(), "oBluetoothAdapter.isDiscovering() "+mBluetoothAdapter.isDiscovering());
                    //(connectException.printStackTrace();
                    if(mmSocket!=null) mmSocket.close();
                    if(mBluetoothAdapter!=null) mBluetoothAdapter.cancelDiscovery();
                } catch (IOException closeException) {
                    Log.e(this.getClass().getCanonicalName(), "ConnectThread->thread->run Failed to close socket");
                    //closeException.printStackTrace();
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
                mmSocket=null;
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), "ConnectThread->thread->cancel Failed to close socket");
            }
        }
    }

    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                if(socket!=null) tmpIn = socket.getInputStream();
                if(socket!=null) tmpOut = socket.getOutputStream();
                Log.v(this.getClass().getCanonicalName(),"Hello Service is Accept");
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), "Error get Stream!");
            } catch (NullPointerException e) {
                Log.e(this.getClass().getCanonicalName(), "Null Error get Stream!");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;



        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            /*Handler mHandler = new Handler(Looper.getMainLooper());

            mHandler.post(new Runnable() {
                public void run() {

                    Toast.makeText(mContext,"Try to Read Message!",Toast.LENGTH_SHORT).show();

                }
            });*/

            int bytes=0;
            // Keep listening to the InputStream until an exception occurs
            while (mState==STATE_CONNECTED) {
                try {

                    // Read from the InputStream
                    if(mmInStream!=null) bytes=mmInStream.read(buffer);
                    //Log.v(this.getClass().getCanonicalName(),"N. of bytes from client: "+bytes);
                    String mMessage = new String(buffer,0,bytes);
                    //BufferedReader bReader=new BufferedReader(new InputStreamReader(mmInStream));
                    //Log.v(this.getClass().getCanonicalName(),"Read String from client: "+mMessage);
                    /*mHandler.post(new Runnable() {
                        public void run() {

                            Toast.makeText(mContext,"Message From Client: "+mMessage,Toast.LENGTH_SHORT).show();

                        }
                    });*/
                    //
                    //Messaggi in Base64
                    mMessage=Base64Coder.decodeString(mMessage);
                    if(mMessage.compareToIgnoreCase("Start Alarm")==0){
                        mRepository.guardLoggger("INFO","Car Alarm Guard Service activate via bluetooth control",0,0,0);
                        mCarAlarmService.activateAlarm();
                    }else if(mMessage.compareToIgnoreCase("Stop Alarm")==0){
                        mRepository.guardLoggger("INFO","Car Alarm Guard Service deactivate via bluetooth control",0,0,0);
                        mCarAlarmService.deActivateAlarm();
                    }else if(mMessage.compareToIgnoreCase("Get Status")==0){

                        write(Base64Coder.encodeString(String.valueOf(mCarAlarmService.mStatus)));
                    }else{
                        mRepository.guardLoggger("INFO","Car Alarm Guard Service test via bluetooth control",0,0,0);
                        mCarAlarmService.testAlarm();
                    }

                } catch (IOException e) {
                    if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(), "Connection Close waiting for nel connection");
                    if(mmInStream!=null) try {
                        mmInStream.close();

                    } catch (IOException e1) {
                        if(Const.IS_DEBUG) Log.w(this.getClass().getCanonicalName(), "Connection Close waiting IOException close Stream for nel connection");
                        e1.printStackTrace();
                    }
                    setState(STATE_NONE);
                    //e.printStackTrace();
                    //Disconnesso il comando mi rimetto in attesa.
                    waitForConnect();
                    break;
                }
            }
        }

        /* Call this from the main Activity to shutdown the connection */
        public void cancel() {
            try {
                if(mmSocket!=null) mmSocket.close();
                mmSocket=null;
                //Try to Reconnect
            } catch (IOException e) {
                Log.e("oBTHelper", "IOException ConnectedThread cancel");
            }
        }
        /**
         * Write to the connected OutStream.
         * @param message  The bytes to write
         */
        public void write(final String message) {
            try {
                /*Handler mHandler = new Handler(Looper.getMainLooper());

                mHandler.post(new Runnable() {
                    public void run() {

                        Toast.makeText(mContext, "Send Message to Server: " + message, Toast.LENGTH_SHORT).show();

                    }
                });*/
                mmOutStream.write(message.getBytes());
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), "Exception during write", e);
            }
        }
    }

    /**
     * qua gestisce la connessione al socket bluetooth aperta
     *
     * */
    public void manageConnectedSocket(BluetoothSocket mmSocket) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }
    /**
     * Chiude la sessione BlueTooth
     * */
    public void disconect() {
        if(mConnectedThread !=null){

            mConnectedThread.cancel();
            mConnectThread.cancel();
            mBluetoothAdapter.cancelDiscovery();
        }

        mConnectedThread =null;
        mConnectThread=null;
        oDevice=null;
    }
    /**Imposto il device Bluetooth*/
    public void setDevice(BluetoothDevice oDeviceBluetooth) {
        oDevice=oDeviceBluetooth;
    }
    public synchronized boolean isbConnect() {
        return bConnect;
    }
}