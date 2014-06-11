package jp.ac.ritsumei.creditapp.view;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;


public class FragmentDays extends Fragment {

    private static DatabaseHelper databaseHelper;

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

//        for (CustomData c : objects) {
//            Log.e("customData", c.getHour() + "," + c.getSubject() + "," + c.getRoom() + "," + c.getAttendNum() + "," + c.isButton());
//        }

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
            CustomData item = getItem(position);

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
                TextView roomText = (TextView) convertView.findViewById(R.id.textViewRoomColumn);
                roomText.setText(item.getRoom());

                //回数
                TextView attendNumText = (TextView) convertView.findViewById(R.id.textViewAttendColumn);
                if (item.getAttendNum() == -1) {
                    attendNumText.setText("");
                } else {
                    attendNumText.setText("" + item.getAttendNum());
                }

                //TODO ボタンの動作
                Button attendButton = (Button) convertView.findViewById(R.id.AttendButton);
                if (item.isButton()) {

                } else {
                    attendButton.setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }
    }


    public List<CustomData> getRowData(Bundle bundle) {
        String sql = "select * from " + DatabaseHelper.TIME_TABLE_NAME +
                " where completed = 0 and day = \"" + getArguments().getString("day") + "\";";


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
                    } else {
                        count++;
                    }
                    objects.add(data);

                } while (cursor.moveToNext());
            } else {//データが１つもない
                for (int i = 1; i <= getArguments().getInt("columnNum"); i++) {
                    objects.add(setNoData(i));
                    count++;
                }
            }

            cursor.close();

            if (count != getArguments().getInt("columnNum")) {
                for (int i = count; i <= getArguments().getInt("columnNum"); i++) {
                    objects.add(setNoData(i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return objects;
    }


    private CustomData setNoData(int columnNum) {
        CustomData data = new CustomData();
        data.setHour(columnNum);
        data.setSubject("");
        data.setRoom("");
        data.setAttendNum(-1);
        data.setButton(false);
        return data;
    }
}
