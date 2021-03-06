package jp.ac.ritsumei.creditapp.sqlite;

import android.content.ContentValues;
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

import jp.ac.ritsumei.creditapp.util.AppConstants;


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
				+ "brand TEXT, parent_brand TEXT,credit INTEGER)",
				/*---------------------------------------------------------------------------------*/
				USER_TABLE_NAME + "("  + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                        + "university TEXT, department TEXT, discipline TEXT, year INTEGER)",
                TIME_TABLE_NAME + "(" + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                        + "term TEXT, day TEXT, hour INTEGER,  subject TEXT, room TEXT,teacher TEXT," +
                        " brand TEXT, attendance INTEGER, credit INTEGER, completed INTEGER)"
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


    /**
     * select文 の入力
     * @param sql
     * @return
     * @throws IOException
     * @throws SQLiteException
     */
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
    synchronized public void insertData(JSONArray scanResults) throws IOException, JSONException {

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
                        "(brand, parent_brand,credit) " +
                        "VALUES(?, ?, ?)");
                for (int i = 1 ; i < scanResults.length() ; i++) {
                    JSONObject datas = (JSONObject) scanResults.get(i);

                    stmt.bindString(1, datas.getString("brand"));
                    stmt.bindString(2, datas.getString("parent_brand"));
                    stmt.bindDouble(3, datas.getInt("credit"));
                    stmt.executeInsert();
                }
             }else if (USER_TABLE_NAME.equals(dbName.getString("table_name"))){
                stmt = db.compileStatement("INSERT INTO " + USER_TABLE_NAME +
                        "(university, department, discipline, year) " +
                        "VALUES(?, ?, ?, ?)");
                 for (int i = 1 ; i < scanResults.length() ; i++) {
                     JSONObject datas = (JSONObject) scanResults.get(i);

                     stmt.bindString(1, datas.getString("university"));
                     stmt.bindString(2, datas.getString("department"));
                     stmt.bindString(3, datas.getString("discipline"));
                     stmt.bindDouble(4, datas.getInt("year"));

                     stmt.executeInsert();
                 }
             }else if (TIME_TABLE_NAME.equals(dbName.getString("table_name"))){
                stmt = db.compileStatement("INSERT INTO " + TIME_TABLE_NAME +
                        "(term , day,hour, subject, room,teacher, brand, attendance, credit,completed) " +
                        "VALUES(?, ?, ?, ?,? ,? ,? ,?,?,?)");
                for (int i = 1 ; i < scanResults.length() ; i++) {
                    JSONObject datas = (JSONObject) scanResults.get(i);

                    stmt.bindString(1, datas.getString("term"));
                    stmt.bindString(2,datas.getString("day"));
                    stmt.bindDouble(3, datas.getInt("hour"));
                    stmt.bindString(4, datas.getString("subject"));
                    stmt.bindString(5, datas.getString("room"));
                    stmt.bindString(6, datas.getString("teacher"));
                    stmt.bindString(7, datas.getString("brand"));
                    stmt.bindDouble(8, datas.getInt("attendance"));
                    stmt.bindDouble(9, datas.getInt("credit"));
                    stmt.bindDouble(10, datas.getInt("completed"));

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


    /**
     * データの削除
     * @param tableName
     * @param where 条件
     * @throws IOException
     */
    public void deleteData(String tableName,String where) throws IOException{
        SQLiteDatabase db = getWritableDatabase();
        if (db.isReadOnly()){
            throw new IOException("Cannot get writable access to DB.");
        }

        SQLiteStatement stmt = null;
        db.beginTransaction();
        try {
            db.delete(tableName,where,null);
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
     * データの更新
     * @param tableName
     * @param where 条件
     * @throws IOException
     */
    public void updateData(String tableName,ContentValues cv,String where) throws IOException{
        SQLiteDatabase db = getWritableDatabase();
        if (db.isReadOnly()){
            throw new IOException("Cannot get writable access to DB.");
        }

        SQLiteStatement stmt = null;
        db.beginTransaction();
        try {
            db.update(tableName,cv,where,null);
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
     * カリキュラム情報の入力
     */
    public void initalInsertRitsData(){

         //情報理工学部
        try {
            //カリキュラムテーブル情報
            insertData(makeCurriculumJSON("立命館大学", "情報理工学部", "", 2012, "卒業単位数"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"外国語科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"教養科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"専門科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"専門基礎科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"共通専門科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"学科専門科目"));
            insertData(makeCurriculumJSON("立命館大学","情報理工学部","",2012,"その他"));

            //区分
            insertData(makeCompositionJSON("卒業単位数","null",124));
            insertData(makeCompositionJSON("外国語科目","卒業単位数",10));
            insertData(makeCompositionJSON("教養科目","卒業単位数",14));
            insertData(makeCompositionJSON("専門科目","卒業単位数",100));
            insertData(makeCompositionJSON("専門基礎科目","専門科目",12));
            insertData(makeCompositionJSON("共通専門科目","専門科目",32));
            insertData(makeCompositionJSON("学科専門科目","専門科目",46));
            insertData(makeCompositionJSON("その他","卒業単位数",0));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //TODO その他学部情報

    }


    public JSONArray makeCurriculumJSON(String univ, String depart, String discipline, int year, String brand){
        JSONArray curriculumJSON = new JSONArray();

        JSONObject table = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            table.put("table_name", CURRICULUM_TABLE_NAME);
            curriculumJSON.put(table);

            data.put("university",univ);
            data.put("department",depart);
            data.put("discipline",discipline);
            data.put("year",year);
            data.put("brand",brand);

            curriculumJSON.put(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  curriculumJSON;
    }


    public JSONArray makeCompositionJSON(String brand, String parent, int credit){
        JSONArray curriculumJSON = new JSONArray();

        JSONObject table = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            table.put("table_name", COMPOSITION_TABLE_NAME);
            curriculumJSON.put(table);

            data.put("parent_brand",parent);
            data.put("credit",credit);
            data.put("brand",brand);

            curriculumJSON.put(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  curriculumJSON;
    }


    public JSONArray makeUserJSON(String univ, String depart, String discipline, int year){
        JSONArray userJSON = new JSONArray();

        JSONObject table = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            table.put("table_name", USER_TABLE_NAME);
            userJSON.put(table);

            data.put("university",univ);
            data.put("department",depart);
            data.put("discipline",discipline);
            data.put("year",year);

            userJSON.put(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  userJSON;
    }

    public JSONArray makeTimeTableJSON(String term,String day,int hour,String subject ,String room
            ,String teacher,String brand,int attendance,int credit,int completed){
        JSONArray timeTableJSON = new JSONArray();

        JSONObject table = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            table.put("table_name", TIME_TABLE_NAME);
            timeTableJSON.put(table);

            data.put("term",term);
            data.put("day",day);
            data.put("hour",hour);
            data.put("subject",subject);
            data.put("room",room);
            data.put("teacher",teacher);
            data.put("brand",brand);
            data.put("attendance",attendance);
            data.put("credit",credit);
            data.put("completed",completed);

            timeTableJSON.put(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  timeTableJSON;
    }



    //TODO あとで消す  登録された時間割 デバッグ用
    public void insertDB(){
        try {
            insertData(makeTimeTableJSON("前期", AppConstants.MONDAY,1,"数学","F201","あ","共通",0,2,0));
            insertData(makeTimeTableJSON("前期",AppConstants.MONDAY,2,"数学1","F201","あ","共通",0,2,0));
            insertData(makeTimeTableJSON("前期",AppConstants.MONDAY,3,"数学2","F201","あ","共通",0,2,0));

            insertData(makeTimeTableJSON("前期",AppConstants.WEDNESDAY,4,"数学あああああああ","F201","あ","共通",0,2,0));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(){
        try {
            insertData(makeUserJSON("立命館大学", "情報理工学部", "", 2012));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}



