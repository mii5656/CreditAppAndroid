package jp.ac.ritsumei.creditapp.main;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.View.OnClickListener;

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
    private Button  b1;
    private String  univ_name,dep_name;
    private Integer year;

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
            settingPref.edit().putBoolean(AppConstants.INSERT_DB_INFO, true).commit();
        }

        //保存されている値を読み出す 第２引数は保存されてなかった場合
        String state = settingPref.getString(AppConstants.SETTING_STATE, AppConstants.STATE_CARRICULUM);

        //TODO 画面遷移
        if (AppConstants.STATE_TIMETABLE.equals(state)) {//時間割画面

        } else if (AppConstants.STATE_CARRICULUM.equals(state)) {//初期画面
            //ボタンの登録、読み込み DBから

            s1 = (Spinner) findViewById(R.id.spinner1);
            s2 = (Spinner) findViewById(R.id.spinner2);
            s2.setEnabled(false);
            s3 = (Spinner) findViewById(R.id.spinner3);
            s3.setEnabled(false);
            b1 = (Button) findViewById(R.id.button1);



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
                    univ_name = (String) spinner.getSelectedItem();

                    // TODO クリックされたときの処理 s2を有効にする s2に学部を読み込む s3リスナーの追加

//                    if (s1.isFocusable() == false) {
//                        s1.setFocusable(true);
//                        return;
//                    } else {
                        s2.setEnabled(true);
                        String sql = "select distinct department from curriculum where university = \"" + univ_name + "\";";


                        try {
                            Cursor cursor = dbHelper.execRawQuery(sql);
                            ArrayAdapter ad2 = new ArrayAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item);

                            if (cursor.moveToFirst()) {
                                do {
                                    ad2.add(cursor.getString(cursor.getColumnIndex("department")));
                                } while (cursor.moveToNext());
                            }


                            s2.setAdapter(ad2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                Spinner spinner = (Spinner) parent;
                                dep_name = (String) spinner.getSelectedItem();

//                                if (s2.isFocusable() == false) {
//                                    s2.setFocusable(true);
//                                    return;
//                                } else {


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

                                    s3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                                        Spinner spinner = (Spinner) parent;
                                        // 選択されたアイテムを取得します
                                        year = Integer.parseInt((String)spinner.getSelectedItem());
                                        }
                                        @Override
                                        public void onNothingSelected(AdapterView<?> arg0) {
                                        }
                                    });

                            }

                        //    }


                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                            }
                        });

                    }
              //  }
                    @Override
                    public void onNothingSelected (AdapterView < ? > arg0){
                    }

            });


            b1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // クリック時の処理


                    try {
                        dbHelper.insertData(dbHelper.makeUserJSON(univ_name, dep_name, "", year));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    startActivity(new Intent(MainActivity.this, TimetableRegistrationActivity.class));
                }
            });



        } else if (AppConstants.STATE_PRESENT_SUBJECT.equals(state)) {//時間割登録画面

        } else {//取得済み科目登録画面

        }

//        startActivity(new Intent(MainActivity.this, TimetableRegistrationActivity.class));
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        s1.setFocusable(false);
//        s2.setFocusable(false);
//        s3.setFocusable(false);
//    }

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