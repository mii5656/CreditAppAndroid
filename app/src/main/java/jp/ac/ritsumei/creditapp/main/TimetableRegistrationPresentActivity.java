package jp.ac.ritsumei.creditapp.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;

public class TimetableRegistrationPresentActivity extends Activity {

    /**
     * 画面全体のテーブル
     */
    TableLayout tableLayout;

    /**
     * データベースヘルパー
     */
    DatabaseHelper databaseHelper;

    /**
     * ユーザの所属情報
     */
    String university;
    String department;
    String discipline;
    int year;


    Map<String, ArrayAdapter> listAdapters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.input_credit);

        TextView textView1 = (TextView) findViewById(R.id.message_text_credit);
        textView1.setText("今学期登録する科目を入力してください");
        TextView textView2 = (TextView) findViewById(R.id.message_text_credit2);
        textView2.setText("今学期登録する科目がない場合は\nそのままにしてください。あとから修正もできます。");


        tableLayout = (TableLayout) findViewById(R.id.register_table);


        databaseHelper = new DatabaseHelper(this);

        makeTableRow(getLayoutInflater(), ((ViewGroup) findViewById(android.R.id.content)));

        Button button = (Button)findViewById(R.id.complete_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences(AppConstants.PREF_NAME,MODE_PRIVATE);
                preferences.edit().putString(AppConstants.SETTING_STATE,AppConstants.STATE_TIMETABLE).commit();
                startActivity(new Intent(TimetableRegistrationPresentActivity.this,TimetableActivity.class));
            }
        });
    }


    /**
     * 授業欄の追加
     */
    private void makeTableRow(LayoutInflater inflater,
                              ViewGroup container) {

        //初期列の追加
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.row_register_curriculum, container, false);
        TextView view = (TextView) tableRow.findViewById(R.id.creditNum);
        view.setText("合計\n単位数");
        Button button = (Button) (tableRow.findViewById(R.id.add_button));
        button.setBackgroundColor(Color.WHITE);
        button.setText("");
        button.setEnabled(false);
        tableLayout.addView(tableRow);

        listAdapters = new HashMap<String, ArrayAdapter>();

        //ユーザ情報の読み込み
        checkUserInfo();

        Cursor cursor = null;
        try {
            cursor = databaseHelper.execRawQuery(makeSQLquery());
            if (cursor.moveToFirst()) {//データがあるとき
                do {
                    tableRow = (TableRow) inflater.inflate(R.layout.row_register_curriculum, container, false);
                    //区分
                    TextView curriculumText = (TextView) tableRow.findViewById(R.id.curriculum_text);
                    String brand = cursor.getString(cursor.getColumnIndex("brand"));
                    curriculumText.setText(brand);

                    //合計単位数
                    TextView creditNumText = (TextView) tableRow.findViewById(R.id.creditNum);
                    creditNumText.setText("0");

                    //登録科目リスト
                    ListView listView = (ListView) tableRow.findViewById(R.id.credit_listView);
                    List<String> strings = new ArrayList<String>();
                    ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, R.layout.row_register_textview, strings);
                    listView.setAdapter(stringArrayAdapter);
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle("登録取り消し")
                                    .setCancelable(true)
                                    .setMessage("授業を取り消しますか?")
                                    .setPositiveButton("はい", new YesDialogClickListener(view.getParent(), position))
                                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //キャンセルボタンが押下された時
                                        }
                                    }).show();
                            return false;
                        }
                    });
                    listAdapters.put(brand, stringArrayAdapter);

                    //登録ボタン
                    Button addButton = (Button) tableRow.findViewById(R.id.add_button);
                    addButton.setOnClickListener(new ButtonClickListner(brand));

                    tableLayout.addView(tableRow);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * sql文の作成
     *
     * @return
     */
    private String makeSQLquery() {
        String sql = "select * from " + DatabaseHelper.CURRICULUM_TABLE_NAME
                + " where university = \"" + university + "\""
                + " and department = \"" + department + "\"";

        if (1 < discipline.length()) {//成績が学部単位か学科単位か
            sql += "and discipline = \"" + discipline + "\"";
        }
        sql += " and year = " + year + " ;";
        return sql;
    }


    /**
     * ユーザテーブルから情報の取得
     */
    private void checkUserInfo() {
        String sql = "select * from " + DatabaseHelper.USER_TABLE_NAME + " ;";

        Cursor cursor = null;
        try {
            cursor = databaseHelper.execRawQuery(sql);
            if (cursor.moveToFirst()) {//データがあるとき
                do {
                    university = cursor.getString(cursor.getColumnIndex("university"));
                    department = cursor.getString(cursor.getColumnIndex("department"));
                    discipline = cursor.getString(cursor.getColumnIndex("discipline"));
                    year = cursor.getInt(cursor.getColumnIndex("year"));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Buttonのリスナー
     */
    private class ButtonClickListner implements View.OnClickListener {
        private final String brand;

        public ButtonClickListner(String brand) {
            this.brand = brand;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            //コンテキストからインフレータを取得
            LayoutInflater inflater = LayoutInflater.from(v.getContext());
            //レイアウトXMLからビュー(レイアウト)をインフレート
            final View registerView = inflater.inflate(R.layout.input_subject_information, null);
            builder.setTitle("授業登録")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //OKボタンが押下された時
                            EditText subjeText = (EditText) registerView.findViewById(R.id.SubjectEditText);
                            EditText teacherText = (EditText) registerView.findViewById(R.id.TeacherEditText);
                            EditText roomText = (EditText) registerView.findViewById(R.id.RoomEditText);

                            Spinner termSpiner = (Spinner) registerView.findViewById(R.id.TermSpiner);
                            Spinner hourSpiner = (Spinner) registerView.findViewById(R.id.HourSpiner);
                            Spinner daysSpiner = (Spinner) registerView.findViewById(R.id.DaysSpiner);
                            Spinner creditSpiner = (Spinner) registerView.findViewById(R.id.CreditSpiner);

                            databaseHelper.insertDataAsyncTask(databaseHelper.makeTimeTableJSON(
                                    (String) termSpiner.getSelectedItem(),
                                    changeDays((String)daysSpiner.getSelectedItem()),
                                    Integer.parseInt((String) hourSpiner.getSelectedItem()),
                                    (subjeText.getText()).toString(),
                                    (roomText.getText()).toString(),
                                    (teacherText.getText()).toString(),
                                    brand,
                                    0,
                                    Integer.parseInt((String) creditSpiner.getSelectedItem()),
                                    0
                            ));
                            ArrayAdapter<String> adapter = listAdapters.get(brand);
                            adapter.add((subjeText.getText()).toString());
                        }
                    })
                    .setView(registerView)
                    .show(); //ダイアログ表示
        }
    }

    private String changeDays(String day){

        if("月".equals(day)){
            return AppConstants.MONDAY;
        }else if("火".equals(day)){
            return AppConstants.TUESDAYDAY;
        }else if("水".equals(day)){
            return AppConstants.WEDNESDAY;
        }else if("木".equals(day)){
            return AppConstants.TUESDAYDAY;
        }else if("金".equals(day)){
            return AppConstants.FRIDAY;
        }else if("土".equals(day)){
            return AppConstants.SATURDAY;
        }else{
            return AppConstants.SUNDAY;
        }
    }

    private class YesDialogClickListener implements DialogInterface.OnClickListener{
        ListView listView;
        int position;

        public YesDialogClickListener(ViewParent parent , int position){
            listView = (ListView) parent;
            this.position = position;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //OKボタンが押下された時
            String item = (String) listView.getItemAtPosition(position);
            try {
                databaseHelper.deleteData(DatabaseHelper.TIME_TABLE_NAME, "subject = \"" + item + "\"");
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
            adapter.remove(item);
        }
    }
}
