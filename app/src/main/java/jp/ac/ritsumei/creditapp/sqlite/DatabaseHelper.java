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
import java.util.Locale;


/**
 * SQLiteを用いて端末内に保存
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	protected static final String 	DEFAULT_DB_FILE_NAME = "credit.db";

	public static final String		CURRICULUM_TABLE_NAME = "curriculum";
	public static final String		COMPOSITION_TABLE_NAME = "composition";
	public static final String		USER_TABLE_NAME = "user";
    public static final String      TIME_TABLE_NAME = "timetable";


	private final String[] DATABASE_TABLE_NAMES;
	public static final SimpleDateFormat SHARED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.JAPANESE);




	private static final String[] DATABASE_DEFINITIONS = {
				/*---------------------------------------------------------------------------------*/
				CURRICULUM_TABLE_NAME + "("  + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "university TEXT, department TEXT, discipline TEXT, year INTEGER, brand TEXT)",
				/*---------------------------------------------------------------------------------*/
				COMPOSITION_TABLE_NAME + "("  + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "brand TEXT, parent_brand TEXT)",
				/*---------------------------------------------------------------------------------*/
				USER_TABLE_NAME + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, "
				+ "sum_credit INTEGER)",
                TIME_TABLE_NAME + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                        + "term TEXT, hour INTEGER,  subject TEXT, room TEXT," +
                        " brand TEXT, attendance INTEGER, credit INTEGER)"
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
				Log.i("createTablesIfNotExist", "CREATE TABLE IF NOT EXISTS " + DATABASE_DEFINITIONS[i]);
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



	synchronized public Cursor execQuery(String tableName, String[] columns, String selection,
			String[] selectionArgs, String sortOrder, String limit) throws IOException, SQLiteException {
		SQLiteDatabase db = getReadableDatabase();
		return db.query(tableName, columns, selection, selectionArgs, null, null, sortOrder, limit);
	}


    synchronized public Cursor execRawQuery(String sql) throws IOException, SQLiteException {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql,null);
    }


    //TODO データベーススキーマの変更があるかも
    /**
     * JSONArrayの先頭にはテーブル名を入れること 'table_name'
     * @param scanResults
     * @throws IOException
     * @throws JSONException
     */
	synchronized protected void insertData(JSONArray scanResults) throws IOException, JSONException {

		SQLiteDatabase db = getWritableDatabase();
		if (db.isReadOnly()){
			throw new IOException("Cannot get writable access to DB.");
		}

		SQLiteStatement stmt = null;
		db.beginTransaction();
		try {
            JSONObject dbName = (JSONObject) scanResults.get(0);

             if(CURRICULUM_TABLE_NAME.equals(dbName.getString("table_name"))){
                 stmt = db.compileStatement("INSERT INTO " + CURRICULUM_TABLE_NAME +
                         "(university, department, discipline, year, brand) " +
                         "VALUES(?, ?, ?, ?,?)");
                 for (int i = 1 ; i < scanResults.length() ; i++) {
                     JSONObject datas = (JSONObject) scanResults.get(i);

                     stmt.bindString(1, datas.getString("university"));
                     stmt.bindString(2, datas.getString("department"));
                     stmt.bindString(3, datas.getString("discipline"));
                     stmt.bindDouble(4, datas.getInt("year"));
                     stmt.bindString(5, datas.getString("brand"));
                     stmt.executeInsert();
                 }
             }else if (COMPOSITION_TABLE_NAME.equals(dbName.getString("table_name"))){
                stmt = db.compileStatement("INSERT INTO " + COMPOSITION_TABLE_NAME +
                        "(brand, parent_brand) " +
                        "VALUES(?, ?)");
                for (int i = 1 ; i < scanResults.length() ; i++) {
                    JSONObject datas = (JSONObject) scanResults.get(i);

                    stmt.bindString(1, datas.getString("brand"));
                    stmt.bindString(2, datas.getString("parent_brand"));
                    stmt.executeInsert();
                }
             }else if (USER_TABLE_NAME.equals(dbName.getString("table_name"))){
                stmt = db.compileStatement("INSERT INTO " + USER_TABLE_NAME +
                        "(sum_credit) " +
                        "VALUES(?)");
                for (int i = 1 ; i < scanResults.length() ; i++) {
                    JSONObject datas = (JSONObject) scanResults.get(i);

                    stmt.bindDouble(1, datas.getInt("sum_credit"));
                    stmt.executeInsert();
                }
             }else if (TIME_TABLE_NAME.equals(dbName.getString("table_name"))){
                stmt = db.compileStatement("INSERT INTO " + TIME_TABLE_NAME +
                        "(term , hour, subject, room, brand, attendance, credit) " +
                        "VALUES(?, ?, ?, ? ,? ,? ,?)");
                for (int i = 1 ; i < scanResults.length() ; i++) {
                    JSONObject datas = (JSONObject) scanResults.get(i);

                    stmt.bindString(1, datas.getString("term"));
                    stmt.bindDouble(2, datas.getInt("hour"));
                    stmt.bindString(3, datas.getString("subject"));
                    stmt.bindString(4, datas.getString("room"));
                    stmt.bindString(5, datas.getString("brand"));
                    stmt.bindDouble(6, datas.getInt("attendance"));
                    stmt.bindDouble(7, datas.getInt("credit"));
                    stmt.executeInsert();
                }
             }else{
                 throw new JSONException("JSONArray[0] is 'table_name' !!");
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


	public void insertDataAsyncTask(JSONArray result){
		new InsertDataTask().execute(result);
	}

	protected class InsertDataTask extends AsyncTask<JSONArray, Integer, Long>{
		@Override
		protected Long doInBackground(JSONArray... params) {
			try {
				insertData(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}




}



