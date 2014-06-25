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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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

        //状態確認用
        settingPref =
                this.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);

        //データベース情報登録確認
        if (!settingPref.getBoolean(AppConstants.INSERT_DB_INFO, false)) {//登録されてない場合
            //初期値入力
            dbHelper.initalInsertRitsData();
            dbHelper.insertDB();
            dbHelper.insertUser();
            settingPref.edit().putBoolean(AppConstants.INSERT_DB_INFO, true).commit();
        }

        //保存されている値を読み出す 第２引数は保存されてなかった場合
        String state = settingPref.getString(AppConstants.SETTING_STATE, AppConstants.STATE_CARRICULUM);

        //TODO 画面遷移
        if (AppConstants.STATE_TIMETABLE.equals(state)) {//時間割画面

        } else if (AppConstants.STATE_CARRICULUM.equals(state)) {//初期画面
            //ボタンの登録、読み込み DBから

            Spinner s1 = (Spinner) findViewById(R.id.spinner1);
            Spinner s2 = (Spinner) findViewById(R.id.spinner2);
            s2.setEnabled(false);
            Spinner s3 = (Spinner) findViewById(R.id.spinner3);
            s3.setEnabled(false);


            try {
                Cursor cursor = dbHelper.execRawQuery("select distinct university from curriculum;");

                ArrayAdapter ad1 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);

                if (cursor.moveToFirst()) {
                    do {
                        //university の単語をスピナーに入れる
                        ad1.add(cursor.getString(cursor.getColumnIndex("university")));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                //s1に項目オブジェクトをセット
                s1.setAdapter(ad1);
            } catch (IOException e) {
                e.printStackTrace();
            }




            //TODO s2 s3 s1にクリックリスナーを付けて、s1が選択されたらs1の大学でs2のセレクト文を実行して のようにどんどん入れ子に

            // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
            s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                //スピナーがクリックされたら呼ばれる
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Spinner spinner = (Spinner) parent;
                    // 選択されたアイテムを取得します
                    String item = (String) spinner.getSelectedItem();

                    // TODO クリックされたときの処理 s2を有効にする s2に学部を読み込む s3リスナーの追加


                }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });



            //TODO 入れる場所が間違ってる

//
//            String univ_name = (String) s1.getSelectedItem();
//            String sql = "select distinct department from curriculum where university = \n" + univ_name + "\n;";
//            Cursor cursor = null;
//
//            try {
//                cursor = dbHelper.execRawQuery(sql);
//
//                ArrayAdapter ad2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
//
//                if (cursor.moveToFirst()) {
//                    do {
//                        ad2.add(cursor.getString(cursor.getColumnIndex("department")));
//                    } while (cursor.moveToNext());
//                }
//
//                //s2に項目オブジェクトをセット
//                s2.setAdapter(ad2);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    Spinner spinner = (Spinner) parent;
//                    String item = (String) spinner.getSelectedItem();
//                    Toast.makeText(MainActivity.this, item, Toast.LENGTH_LONG).show();
//
//
//                }
//                @Override
//                public void onNothingSelected(AdapterView<?> arg0) {
//                }
//            });
//
//
//
//
//            String dep_name = (String) s2.getSelectedItem();
//            String sql2 = "select distinct year from curriculum where department = \n" + dep_name+ "\n;";
//            cursor = null;
//
//            try {
//                cursor = dbHelper.execRawQuery(sql2);
//
//                ArrayAdapter ad3 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item);
//
//                if (cursor.moveToFirst()) {
//                    do {
//                        ad3.add(cursor.getString(cursor.getColumnIndex("year")));
//                    } while (cursor.moveToNext());
//                }
//                cursor.close();
//                //s3に項目オブジェクトをセット
//                s3.setAdapter(ad3);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }





        } else if (AppConstants.STATE_PRESENT_SUBJECT.equals(state)) {//時間割登録画面

        } else {//取得済み科目登録画面

        }

//        startActivity(new Intent(MainActivity.this, TimetableRegistrationActivity.class));
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
}