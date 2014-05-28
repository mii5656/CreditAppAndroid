package jp.ac.ritsumei.creditapp.main;

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


public class MainActivity extends ActionBarActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO sqliteのチェック
        checkSQLite();
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
        //dataベースヘルパーを作る
        dbHelper = new DatabaseHelper(this.getApplication());

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