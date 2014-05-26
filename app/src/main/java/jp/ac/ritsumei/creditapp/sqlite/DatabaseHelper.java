package jp.ac.ritsumei.creditapp.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * SQLiteを用いて端末内に保存
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String 	DEFAULT_DB_FILE_NAME = "credit.db";
	protected String  DB_NAME;
	

	public static final String		CURRICULUM_TABLE_NAME			= "curriculum";
	public static final String		COMPOSITION_TABLE_NAME = "composition";
	public static final String		USER_TABLE_NAME = "user";


	private final String[] DATABASE_TABLE_NAMES;
	public static final SimpleDateFormat SHARED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.JAPANESE);


	private static final String[] DATABASE_DEFINITIONS = {
				/*---------------------------------------------------------------------------------*/
				CURRICULUM_TABLE_NAME + "("  + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "time TEXT, value TEXT )",
				/*---------------------------------------------------------------------------------*/
				COMPOSITION_TABLE_NAME + "("  + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "time TEXT, value TEXT )",
				/*---------------------------------------------------------------------------------*/
				USER_TABLE_NAME + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "time TEXT, essid TEXT, bssid TEXT, lat REAL,lon REAL,  maxrssi INTEGER )"
	};


	/**
	 * デフォルトのコンストラクタ
	 * @param context
	 */
	public DatabaseHelper(Context context) {
		super(context, DEFAULT_DB_FILE_NAME, null, 1);
		ArrayList<String> list = new ArrayList<String>();
		Field[] fields = DatabaseHelper.class.getDeclaredFields();
		for(int i = 0 ; i < fields.length ; i++){
			if(fields[i].getName().endsWith("_TABLE_NAME") &&
					String.class.equals(fields[i].getType())){
				try {
					list.add(String.valueOf(fields[i].get(null)));
				} catch (IllegalArgumentException e) {
					Log.e(getClass().getSimpleName(), "This is a bug.", e);
					System.exit(-1);
				} catch (IllegalAccessException e) {
					Log.e(getClass().getSimpleName(), "This is a bug.", e);
					System.exit(-1);
				}
			}
		}
		DATABASE_TABLE_NAMES = list.toArray(new String[0]);
	}


	@Override
	synchronized public void onCreate(SQLiteDatabase db) {
		createTablesIfNotExist(db);
	}

    /**
     *
     * @param sqlitedatabase
     */
	private void createTablesIfNotExist(SQLiteDatabase sqlitedatabase) {
		try {
			dropTablesIfExist(sqlitedatabase);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for(int i = 0 ; i < DATABASE_DEFINITIONS.length ; i++){
			try {
				Log.e("createTablesIfNotExist", "CREATE TABLE IF NOT EXISTS " + DATABASE_DEFINITIONS[i]);
				sqlitedatabase.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_DEFINITIONS[i]);
			}catch (IllegalStateException e) {
				Log.w(getClass().getSimpleName(), 
						"perhaps, service was restarted or un/reinstalled.", e);
			}
		}
	}


    /**
     *
     * @param db
     * @throws IOException
     */
	protected void dropTablesIfExist(SQLiteDatabase db) throws IOException {
		if (db.isReadOnly()){
			throw new IOException("Cannot get writable access to DB.");
		}
		for (int i = 0 ; i < DATABASE_TABLE_NAMES.length ; i++) {
			try {
				Log.w("dropTablesIfExist", DATABASE_TABLE_NAMES[i]);
				db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NAMES[i]);
			}catch (IllegalStateException e) {
				Log.w(getClass().getSimpleName(), 
						"perhaps, service was restarted or un/reinstalled.", e);
			}
		}
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	synchronized public Cursor selectRawData(String tableName, String[] columns, String selection,
			String[] selectionArgs, String sortOrder, String limit) throws IOException, SQLiteException {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(tableName, columns, selection, selectionArgs, null, null, sortOrder, limit);
	}












	synchronized protected void insertWifiDataMAX(JSONArray scanResults) throws IOException, JSONException {

		SQLiteDatabase db = getWritableDatabase();
		if (db.isReadOnly()){
			throw new IOException("Cannot get writable access to DB.");
		}

		SQLiteStatement stmt = null;
		db.beginTransaction();
		try {
			stmt = db.compileStatement("INSERT INTO " + CURRICULUM_TABLE_NAME +
					"(time, essid, bssid, lat, lon, rssi) VALUES(?, ?, ?, ?,?,?)");

			for (int i = 0 ; i < scanResults.length() ; i++) {
				JSONObject ap = (JSONObject) scanResults.get(i);

				stmt.bindString(1, SHARED_DATE_FORMAT.format(new Date(ap.getLong("time"))));

				stmt.bindString(2, ap.getString("essid"));
				stmt.bindString(3, ap.getString("bssid"));
				stmt.bindDouble(4, ap.getDouble("lat"));
				stmt.bindDouble(5, ap.getDouble("lon"));
				stmt.bindLong(6, ap.getInt("rssi"));
				stmt.executeInsert();
			}
			db.setTransactionSuccessful();
		}catch (IllegalStateException e) {
			Log.w(getClass().getSimpleName(), 
					"perhaps, service was restarted or un/reinstalled.", e);
		}finally {

			db.endTransaction();
			if(stmt != null){
				stmt.clearBindings();
				stmt.close();
			}
		}
	}


	/**
	 * insert the magneticfieled table
	 * @param scanResults
	 * @throws java.io.IOException
	 * @throws org.json.JSONException
	 */
	synchronized protected void insertMagneticFieled(long time , JSONArray scanResults)
			throws IOException, JSONException {
		SQLiteDatabase db = getWritableDatabase();
		if (db.isReadOnly()) {
			throw new IOException("Cannot get writable access to DB.");
		}

		SQLiteStatement stmt = null;
		db.beginTransaction();

		try {
			stmt = db.compileStatement("INSERT INTO "
					+ CURRICULUM_TABLE_NAME
					+ "(time,value) VALUES(?, ?)");

			stmt.bindString(1,SHARED_DATE_FORMAT.format(new Date(time)));//SHARED_DATE_FORMAT.format(new Date(scanResults.getLong("time"))));
			stmt.bindString(2,scanResults.toString());
			stmt.executeInsert();

			db.setTransactionSuccessful();

		} catch (IllegalStateException e) {
			Log.w(getClass().getSimpleName(),
					"perhaps, service was restarted or un/reinstalled.", e);
		} finally {

			db.endTransaction();
			if (stmt != null) {
				stmt.clearBindings();
				stmt.close();
			}
		}
	}


	/**
	 * insert wifi max data using asyncTask
	 * @param result
	 */
	public void insertWiFiDataMAXAsyncTask(JSONArray result){
		new InsertWiFiDataMAXTask().execute(result);
	}




	/**
	 * insert wifi data (max rssi)
	 * logged data will be insert to DB by async task to decrease the process time in UI thread
	 * and logging data will be add a buffer to decrease the times of inserting
	 */
	protected class InsertWiFiDataMAXTask extends AsyncTask<JSONArray, Integer, Long>{

		@Override
		protected Long doInBackground(JSONArray... params) {
			try {
				insertWifiDataMAX(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}



