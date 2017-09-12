package security.utils;

/**
 * Created by gianluca on 03/10/13.
 */
public class Const {
    public static final int STATUS_RUNNING=0;
    public static final int STATUS_TESTING=1;
    public static final int STATUS_STOP=2;
    public static final int STATUS_ENDED=-1;
    public static final long DELAY_BLUETOOTH = 0;
    public static final long PERIOD_BLUETOOTH = 300000;
    public static final String FOLDER_ALARM = "carAlarm";



    public static boolean IS_DEBUG =true;

    public static final  String SHARED_PREFS = "carAlarm";

    public static final int DELAY_ALARM_START = 3000;
    public static final int REPEAT_ALARM = 90000;
    public static final int PERMISSION_LOCATION_REQUEST_CODE=1;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 2;
    public static final int PERMISSION_CALL_PHONE_CODE = 3;
}
