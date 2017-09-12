package security.utils.db;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
	private static DatabaseHelper mDbHelper;
	private static SQLiteDatabase mDb;
	private static String DB_PATH = "/data/data/%PACKAGE%/databases/";
	private static final String DATABASE_NAME = "CarAlarm";
	private static final int DB_VERSION = 1;
	private static Context adapterContext;
	private static String mDbFullPath;
	public DBAdapter(Context context) {
		this.adapterContext = context;
	}

	public synchronized DBAdapter open()  {
		mDbHelper = new DatabaseHelper(adapterContext);

		try {
			mDbHelper.checkDatabase();
		} catch (NullPointerException e) {
			Log.e( this.getClass().getCanonicalName(), "NullPointer nell' open() quando faccio .createDataBase()");
		}

		try {
			mDbHelper.openDataBase();
			//Log.v( this.getClass().getCanonicalName(), "Database aperto");
		} catch (SQLException e) {
			Log.e( this.getClass().getCanonicalName(), "Unable to open database");

		}
        return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public Cursor fetchAll(String table) {
		return mDb.query(table, null,null,null,null,null,null);              
	}
	
	public SQLiteDatabase getmDb() {
		return this.mDb;
	}

	public void setmDb(SQLiteDatabase oDb) {
		this.mDb = oDb;
	}


	private static class DatabaseHelper extends SQLiteOpenHelper {
	
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DB_VERSION);

			DB_PATH=DB_PATH.replace("%PACKAGE%", context.getPackageName());

			File fPath = new File(DB_PATH);
			if(!fPath.exists()) fPath.mkdir();
			//Log.v(getClass().getName(), "new DatabaseHelper: DB_NAME="+DATABASE_NAME+" DB_PATH="+DB_PATH);
		}


		public void createDB(SQLiteDatabase db) {
			try {
		
				/***
				 * 
				 * Table Log
				 * 
				 */
				String creationGuardTable = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
						"{2} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
						"{3} VARCHAR(20) NOT NULL, " +
						"{4} VARCHAR(255) NOT NULL, " +
                        "{5} DOUBLE NOT NULL DEFAULT 0, " +
                        "{6} DOUBLE NOT NULL DEFAULT 0, " +
                        "{7} DOUBLE NOT NULL DEFAULT 0)";

				db.execSQL(MessageFormat.format(creationGuardTable, CarAlarmGuardTable.TABLE_NAME, CarAlarmGuardTable.LOGID,
                        CarAlarmGuardTable.LOGDATE, CarAlarmGuardTable.LOGTYPE, CarAlarmGuardTable.LOGDESC,
                        CarAlarmGuardTable.LOGLONGITUDE, CarAlarmGuardTable.LOGLATIDUTE ,CarAlarmGuardTable.LOGALTIDUTE));

				creationGuardTable = "CREATE TABLE IF NOT EXISTS {0} " +
						"({1} VARCHAR(20) NOT NULL," +
						"{2} BOOLEAN        DEFAULT 0 )";

				db.execSQL(MessageFormat.format(creationGuardTable, CarAlarmPrefTable.TABLE_NAME, CarAlarmPrefTable.PREFS,
						CarAlarmPrefTable.VALUE));

				ContentValues tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "bluetooth");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

				tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "accererometer");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

				tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "gps");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

				tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "sms");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

				tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "call");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

				tableLogValue = new ContentValues();
				tableLogValue.put(CarAlarmPrefTable.PREFS, "service");
				tableLogValue.put(CarAlarmPrefTable.VALUE, false);
				db.insertOrThrow(CarAlarmPrefTable.TABLE_NAME, null, tableLogValue);

			} catch (SQLiteException e) {
				Log.e( this.getClass().getCanonicalName(), "Errore nelle stringhe di creazione del db");
				e.printStackTrace();
				//Cancello il DB se la creazione 
				File fdb = new File(mDbFullPath);
				if(fdb.exists()) fdb.delete();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}
		
		/**
		 * Questo metodo controlla se il database esiste oppure no, se esiste lo fa ritornare
		 * @author coluzza
		 * @return SQLiteDatabase
		 *
		 */
		public SQLiteDatabase checkDatabase(){
			SQLiteDatabase checkDB = null;

			try {
				mDbFullPath = DB_PATH + DATABASE_NAME;
				
				File fdb = new File(mDbFullPath);
				this.checkFileDB(fdb);
				
				checkDB = SQLiteDatabase.openDatabase(mDbFullPath, null, SQLiteDatabase.OPEN_READWRITE);
				
				
			} catch (SQLiteException e) {
				Log.e( this.getClass().getCanonicalName(), "Errore nel checkDatabase()");

			}

			if (checkDB != null) {
				checkDB.close();
				//Log.v( this.getClass().getCanonicalName(), "checkDB diverso da null nel checkDataBase");
			}

			return checkDB;
		}
		
		/**
		 * Questo metodo controlla che il file del db esiste, se non esiste lo apre per essere usato in scrittura e
		 * poi lo crea con le tabelle del metodo createDB
		 * @author coluzza
		 * @param filedb
		 */
		private void checkFileDB(File filedb) {
			if(!filedb.exists()){
                //Log.v( this.getClass().getCanonicalName(), "DB not Exist try to Create");
                mDb = SQLiteDatabase.openDatabase(mDbFullPath, null, SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.OPEN_READWRITE);
				this.createDB(mDb);
				
				if (mDb!=null){
					//Log.v( this.getClass().getCanonicalName(), "mDb diverso da null nel checkFileDb");
					mDb.close();
				}
			}
		/*	int i = 10;
			
			while (!filedb.exists() && i > 0) {
				i--;
				
				try {
					mDb = this.getWritableDatabase();
					this.createDB(mDb);
					
					if (mDb!=null)
						Log.v(TAG, "mDb diverso da null nel checkFileDb");
				} catch (SQLiteException e) {
					Log.e(TAG, "Errore nel createDataBase()");
					throw new PersistenceException(e.getMessage());
				} finally {
					mDb.close();
				}
			}*/
		}

		/**
		 * Questo metodo apre il database
		 * @throws java.sql.SQLException
		 */
		public synchronized void openDataBase() throws SQLException {
			String myPath = DB_PATH + DATABASE_NAME;
			if(mDb!=null) mDb.close();
            mDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

		}
		
		/**
		 * Questo metodo chiude il database
		 */
		@Override
		public synchronized void close() {

			if (mDb != null)
				mDb.close();

			super.close();

		}


		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub

		}
	}
}