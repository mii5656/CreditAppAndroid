package jp.ac.ritsumei.creditapp.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;


public class MainActivity extends ActionBarActivity {

    private DatabaseHelper dbHelper;
    private SharedPreferences settingPref;
    private Spinner s1;
    private Spinner s2;
    private Spinner s3;
    private Button b1;
    private String univ_name, dep_name;
    private Integer year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //dataベースヘルパーを作る
        dbHelper = new DatabaseHelper(this.getApplication());

        //状態確認用
        settingPref = this.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);

        //データベース情報登録確認
        if (!settingPref.getBoolean(AppConstants.INSERT_DB_INFO, false)) {//登録されてない場合
            //初期値入力
            dbHelper.initalInsertRitsData();
            settingPref.edit().putBoolean(AppConstants.INSERT_DB_INFO, true).commit();
        }

        //保存されている値を読み出す 第２引数は保存されてなかった場合
        String state = settingPref.getString(AppConstants.SETTING_STATE, AppConstants.STATE_CARRICULUM);

        if (AppConstants.STATE_TIMETABLE.equals(state)) {//時間割画面
            startActivity(new Intent(MainActivity.this, TimetableActivity.class));
        } else if (AppConstants.STATE_CARRICULUM.equals(state)) {//初期画面
            setSpiners();
        } else if (AppConstants.STATE_PRESENT_SUBJECT.equals(state)) {//時間割登録画面
            startActivity(new Intent(MainActivity.this, TimetableRegistrationPresentActivity.class));
        } else {//取得済み科目登録画面
            startActivity(new Intent(MainActivity.this, TimetableRegistrationActivity.class));
        }
    }



    /**
     * スピナーをセット
     */
    private void setSpiners() {
        //ボタンの登録、読み込み DBから
        s1 = (Spinner) findViewById(R.id.spinner1);
        s2 = (Spinner) findViewById(R.id.spinner2);
        s2.setEnabled(false);
        s3 = (Spinner) findViewById(R.id.spinner3);
        s3.setEnabled(false);
        b1 = (Button) findViewById(R.id.button1);

        try {
            Cursor cursor = dbHelper.execRawQuery("select distinct university from curriculum;");

            ArrayAdapter ad1 = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item);
            ad1.add("大学");
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
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        s1.setOnItemSelectedListener(new SpinnerListener1());
        s2.setOnItemSelectedListener(new SpinnerListener2());
        s3.setOnItemSelectedListener(new SpinnerListener3());
        s1.setFocusable(false);
        s2.setFocusable(false);

        //登録Buttonのリスナーを登録
        b1.setOnClickListener(new RegisterButtonListener());
    }


    /**
     * スピナー1のリスナー
     */
    private class SpinnerListener1 implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            Spinner spinner = (Spinner) parent;

            if (spinner.isFocusable() == false) { // 初回起動時の動作 ※2
                spinner.setFocusable(true);
                return;
            } else {

                // 選択されたアイテムを取得します
                univ_name = (String) spinner.getSelectedItem();

                s2.setEnabled(true);
                String sql = "select distinct department from curriculum where university = \"" + univ_name + "\";";

                try {
                    Cursor cursor = dbHelper.execRawQuery(sql);
                    ArrayAdapter ad2 = new ArrayAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item);

                    ad2.add("学部");
                    if (cursor.moveToFirst()) {
                        do {
                            ad2.add(cursor.getString(cursor.getColumnIndex("department")));
                        } while (cursor.moveToNext());
                    }
                    s2.setAdapter(ad2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    /**
     * スピナー2のリスナー
     */
    private class SpinnerListener2 implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            if (spinner.isFocusable() == false) { // 初回起動時の動作 ※2
                spinner.setFocusable(true);
                return;
            } else {


                dep_name = (String) spinner.getSelectedItem();

                s3.setEnabled(true);
                String sql = "select distinct year from curriculum where department = \"" + dep_name + "\";";

                try {
                    Cursor cursor = dbHelper.execRawQuery(sql);

                    ArrayAdapter ad3 = new ArrayAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item);

                    if (cursor.moveToFirst()) {
                        do {
                            ad3.add(cursor.getString(cursor.getColumnIndex("year")));
                        } while (cursor.moveToNext());
                    }
                    cursor.close();

                    //s3に項目オブジェクトをセット
                    s3.setAdapter(ad3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                year = null;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    /**
     * スピナー3のリスナー
     */
    private class SpinnerListener3 implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            // 選択されたアイテムを取得します
            year = Integer.parseInt((String) spinner.getSelectedItem());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    /**
     * 登録ボタンのリスナー
     */
    private class RegisterButtonListener implements View.OnClickListener {
        public void onClick(View v) {
            // クリック時の処理
            if (year != null && dep_name != null && !"学部".equals(dep_name)) {
                try {
                    dbHelper.insertData(dbHelper.makeUserJSON(univ_name, dep_name, "", year));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                settingPref.edit().putString(AppConstants.SETTING_STATE, AppConstants.STATE_COMPLETED_SUBJECT).commit();
                startActivity(new Intent(MainActivity.this, TimetableRegistrationActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "入力が間違っています。", Toast.LENGTH_LONG).show();
            }
        }
    }
}