package security.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import security.utils.Const;

/**
 * Created by gianluca on 31/12/15.
 */
public class Logger {

    private static String  mDirectory="com.glm.caralarm";

    private static String mLogFile="carAlarm.log";
    /**
     * Write to txt log file for debug
     * */
    public static void log(final String message){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm.ss.SSS");
                SimpleDateFormat sdfLog = new SimpleDateFormat("yyyyMMdd");
                mLogFile="carAlarm-"+sdfLog.format(new Date())+".log";
                Log.i(this.getClass().getCanonicalName(),"Logger to Service log");

                File root = Environment.getExternalStorageDirectory();

                File dir = new File(root.getAbsolutePath() + "/" + Const.FOLDER_ALARM);
                //Create app to log
                if(!dir.exists()){
                    dir.mkdir();
                    dir.mkdirs();
                }

                dir = new File(root.getAbsolutePath() + "/" + Const.FOLDER_ALARM +"/"+ mDirectory);

                //Create dir to log
                if(!dir.exists()){
                    dir.mkdir();
                    dir.mkdirs();
                }
                File logFile = new File(dir, mLogFile);

                try {
                    FileOutputStream f = new FileOutputStream(logFile,true);
                    PrintWriter pw = new PrintWriter(f);
                    pw.println(sdf.format(new Date())+" --> "+message);
                    pw.flush();
                    pw.close();
                    f.close();
                    // Log.v(TAG, "file written to sd card");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    // Log.i(TAG, "******* File not found. Did you" +
                    // " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}