package jp.ac.ritsumei.creditapp.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;


public class MainActivity extends ActionBarActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences settingPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dataベースヘルパーを作る
        dbHelper = new DatabaseHelper(this.getApplication());

        //TODO sqliteのチェック
        checkSQLite();


        //状態確認用
         settingPref =
                this.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE );

        //データベース情報登録確認
        if(!settingPref.getBoolean(AppConstants.INSERT_DB_INFO,false)){//登録されてない場合
            //初期値入力
            dbHelper.initalInsertRitsData();
            dbHelper.insertDB();
            settingPref.edit().putBoolean(AppConstants.INSERT_DB_INFO,true).commit();
        }


        //保存されている値を読み出す 第２引数は保存されてなかった場合
        String state = settingPref.getString(AppConstants.SETTING_STATE, AppConstants.STATE_CARRICULUM);

        //TODO 画面遷移
        if(AppConstants.STATE_TIMETABLE.equals(state)){//時間割画面

        }else if(AppConstants.STATE_CARRICULUM.equals(state)){//初期画面
             //ボタンの登録、読み込み DBから

        }else if(AppConstants.STATE_PRESENT_SUBJECT.equals(state)){//時間割登録画面

        }else{//取得済み科目登録画面

        }

        startActivity(new Intent(MainActivity.this,TimetableActivity.class));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void checkSQLite(){

        //dataを入れる。
        JSONArray datum = new JSONArray();
        JSONObject table = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            table.put("table_name", DatabaseHelper.USER_TABLE_NAME);
            datum.put(table);
            data.put("sum_credit",3);
            datum.put(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dbHelper.insertDataAsyncTask(datum);

        //dataを読み込む = select分の実行と読み込み
        try {
            Cursor cursor = dbHelper.execRawQuery("select * from "+DatabaseHelper.USER_TABLE_NAME+";");
            Log.e("inserted data is " , ""+1);
            if(cursor.moveToFirst()){
                do{
                    int sum_credit = cursor.getInt(cursor.getColumnIndex("sum_credit"));
                    Log.e("inserted data is " , ""+sum_credit);
                }while(cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}