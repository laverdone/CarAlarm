package security.utils.db;


import android.provider.BaseColumns;

/**
 * Created by gianluca on 28/01/16.
 */
public interface CarAlarmPrefTable extends BaseColumns {

    String TABLE_NAME = "prefs";

    String PREFS = "prefs";

    String VALUE = "value";

    String[] COLUMNS = new String[] {PREFS, VALUE};

}
