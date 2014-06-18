package jp.ac.ritsumei.creditapp.view;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;


public class FragmentDays extends Fragment {

    private static DatabaseHelper databaseHelper;

    private Map<String,TextView> attendTextViews;

    /**
     * フラグメントを作成するためのファクトリメソッド
     */
    public static FragmentDays newInstance(String day, int colunmNum, DatabaseHelper db) {
        FragmentDays fragment = new FragmentDays();

        Bundle args = new Bundle();
        args.putString("day", day);
        args.putInt("columnNum", colunmNum);
        // フラグメントに渡す値をセット
        fragment.setArguments(args);

        if (databaseHelper == null) {
            databaseHelper = db;
        }

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View linearLayout = inflater.inflate(R.layout.fragment_days, container, false);
        ListView listView = (ListView) linearLayout.findViewById(R.id.list_day_view);

        List<CustomData> objects = getRowData(savedInstanceState);

        attendTextViews = new HashMap<String, TextView>();

        CustomAdapter customAdapater = new CustomAdapter(getActivity(), 0, objects);

        listView.setAdapter(customAdapater);

        return linearLayout;
    }


    /**
     * リストビュー用アダプタ
     */
    public class CustomAdapter extends ArrayAdapter<CustomData> {
        private LayoutInflater layoutInflater_;

        public CustomAdapter(Context context, int textViewResourceId, List<CustomData> objects) {
            super(context, textViewResourceId, objects);
            layoutInflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 特定の行(position)のデータを得る
            final CustomData item = getItem(position);

            // convertViewは使い回しされている可能性があるのでnullの時だけ新しく作る
            if (null == convertView) {
                convertView = layoutInflater_.inflate(R.layout.row, null);


                // CustomDataのデータをViewの各Widgetにセットする 時限
                TextView hourText = (TextView) convertView.findViewById(R.id.textViewHourColumn);
                hourText.setText("" + item.getHour());

                //科目
                TextView subjText = (TextView) convertView.findViewById(R.id.textViewSubjColumn);
                subjText.setText(item.getSubject());

                //教室
                final TextView roomText = (TextView) convertView.findViewById(R.id.textViewRoomColumn);
                roomText.setText(item.getRoom());

                //回数
                TextView attendNumText = (TextView) convertView.findViewById(R.id.textViewAttendColumn);
                if (item.getAttendNum() == -1) {
                    attendNumText.setText("");
                } else {
                    attendNumText.setText("" + item.getAttendNum());
                }
                attendTextViews.put(item.getSubject(),attendNumText);


                Button attendButton = (Button) convertView.findViewById(R.id.AttendButton);
                if (item.isButton()) {
                    attendButton.setOnClickListener(
                            new attendButtonClickListner(item.getAttendNum(),item.getSubject())
                    );
                } else {
                    attendButton.setVisibility(View.INVISIBLE);
                }
            }
            return convertView;
        }
    }





    /**
     * リストビュー用の行用のカスタムデータの取得
     * @param bundle
     * @return
     */
    public List<CustomData> getRowData(Bundle bundle) {
        String sql = "select * from " + DatabaseHelper.TIME_TABLE_NAME +
                " where completed = 0 and day = \"" + getArguments().getString("day") + "\" order by hour;";


        List<CustomData> objects = new ArrayList<CustomData>();

        Cursor cursor = null;
        try {
            int count = 1;
            cursor = databaseHelper.execRawQuery(sql);

            if (cursor.moveToFirst()) {//データがあるとき
                do {
                    CustomData data = new CustomData();
                    data.setHour(cursor.getInt(cursor.getColumnIndex("hour")));
                    data.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
                    data.setRoom(cursor.getString(cursor.getColumnIndex("room")));
                    data.setAttendNum(cursor.getInt(cursor.getColumnIndex("attendance")));
                    data.setButton(true);

                    if (count < data.getHour()) {
                        for (int j = count; j < data.getHour(); j++) {
                            objects.add(setNoData(j));
                            count++;
                        }
                    }
                    objects.add(data);
                    count++;
                } while (cursor.moveToNext());

            } else {//データが１つもない
                for (int i = 1; i <= getArguments().getInt("columnNum"); i++) {
                    objects.add(setNoData(i));
                    count++;
                }
            }

            cursor.close();

            if (count <= getArguments().getInt("columnNum")) {
                for (int i = count; i <= getArguments().getInt("columnNum"); i++) {
                    objects.add(setNoData(i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return objects;
    }


    /**
     * 何もないデータのセット
     * @param columnNum
     * @return
     */
    private CustomData setNoData(int columnNum) {
        CustomData data = new CustomData();
        data.setHour(columnNum);
        data.setSubject("");
        data.setRoom("");
        data.setAttendNum(-1);
        data.setButton(false);
        return data;
    }

    /**
     * 出席カウント用ダイアログリスナー
     */
    private class YesDialogClickListener implements DialogInterface.OnClickListener{
       int attendNum;
       String subjName;

        public YesDialogClickListener(int attend,String suj){
            this.attendNum = attend;
            this.subjName = suj;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //OKボタンが押下された時
            ContentValues cv = new ContentValues();
            cv.put("attendance",attendNum);

            try {
                databaseHelper.updateData(DatabaseHelper.TIME_TABLE_NAME, cv, "subject = \"" + subjName + "\"");

                attendTextViews.get(subjName).setText(""+attendNum);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 出席カウント用ボタンリスナー
     */
    private class attendButtonClickListner implements View.OnClickListener{

        int attend;
        String subj;

        public attendButtonClickListner(int attend,String subj){
            this.attend = attend;
            this.subj = subj;
        }

        @Override
        public void onClick(View v) {
            attend++;
            //出席ダイアログ
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("出席")
                    .setCancelable(true)
                    .setMessage("出席回数を増やしますか?")
                    .setPositiveButton("はい", new YesDialogClickListener(attend,subj))
                    .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //キャンセルボタンが押下された時
                        }
                    }).show();
        }
    }
}
