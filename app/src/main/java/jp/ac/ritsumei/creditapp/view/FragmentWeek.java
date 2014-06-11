package jp.ac.ritsumei.creditapp.view;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;

public class FragmentWeek extends Fragment {

    private static DatabaseHelper databaseHelper;

    /**
     * フラグメントを作成するためのファクトリメソッド
     */
    public static FragmentDays newInstance(int colunmNum, boolean hasSaturDay, boolean hasSunDay, DatabaseHelper db) {
        FragmentDays fragment = new FragmentDays();

        Bundle args = new Bundle();
        args.putInt("columnNum", colunmNum);
        args.putBoolean("hasSaturDay", hasSaturDay);
        args.putBoolean("hasSunDay", hasSunDay);

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

        View linearLayout = inflater.inflate(R.layout.fragment_week, container, false);

        TableLayout tableLayout = (TableLayout) linearLayout.findViewById(R.id.week_table);

        List<TableRow> rows = makeTableRow(getArguments().getInt("columnNum")
                , getArguments().getBoolean("hasSaturDay"), getArguments().getBoolean("hasSunDay"));

        for (TableRow row : rows) {
            tableLayout.addView(row);
        }
        Log.e("aaaaaaaaa", "view");
        return linearLayout;
    }

    private List<TableRow> makeTableRow(int colunmNum, boolean hasSaturDay, boolean hasSunDay) {
        List<TableRow> rows = new ArrayList<TableRow>();

        String sql = "select * from " + DatabaseHelper.TIME_TABLE_NAME +
                " where completed = 0 and hour = ";

        for (int i = 1; i <= colunmNum; i++) {

            TableRow tableRow = new TableRow(getActivity());

            //初期化
            TextView monText = new TextView(getActivity());
            monText.setText("");
            TextView tueText = new TextView(getActivity());
            tueText.setText("");
            TextView wedText = new TextView(getActivity());
            wedText.setText("");
            TextView thurText = new TextView(getActivity());
            thurText.setText("");
            TextView friText = new TextView(getActivity());
            friText.setText("");
            TextView satText = new TextView(getActivity());
            satText.setText("");
            TextView sunText = new TextView(getActivity());
            sunText.setText("");


            Cursor cursor = null;
            try {
                sql += i + ";";
                cursor = databaseHelper.execRawQuery(sql);

                if (cursor.moveToFirst()) {//データがあるとき
                    do {
                        String day = cursor.getString(cursor.getColumnIndex("day"));
                        if (AppConstants.MONDAY.equals(day)) {
                            monText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.TUESDAYDAY.equals(day)) {
                            tueText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.WEDNESDAY.equals(day)) {
                            wedText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.TUESDAYDAY.equals(day)) {
                            thurText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.FRIDAY.equals(day)) {
                            friText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.SATURDAY.equals(day)) {
                            satText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        } else if (AppConstants.SUNDAY.equals(day)) {
                            sunText.setText(cursor.getString(cursor.getColumnIndex("subject")));
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            tableRow.addView(monText);
            tableRow.addView(tueText);
            tableRow.addView(wedText);
            tableRow.addView(thurText);
            tableRow.addView(friText);
            if (hasSaturDay) {
                tableRow.addView(satText);
            }
            if (hasSunDay) {
                tableRow.addView(sunText);
            }

            rows.add(tableRow);
        }
        return rows;
    }

}
