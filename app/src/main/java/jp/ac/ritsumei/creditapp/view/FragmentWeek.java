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
    public static FragmentWeek newInstance(int colunmNum, boolean hasSaturDay, boolean hasSunDay, DatabaseHelper db) {
        FragmentWeek fragment = new FragmentWeek();

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

        List<TableRow> rows = makeTableRow(inflater, container, getArguments().getInt("columnNum")
                , getArguments().getBoolean("hasSaturDay"), getArguments().getBoolean("hasSunDay"));

        for (TableRow row : rows) {
            tableLayout.addView(row);
        }

        return linearLayout;
    }

    /**
     * 行の作成
     *
     * @param colunmNum
     * @param hasSaturDay
     * @param hasSunDay
     * @return
     */
    private List<TableRow> makeTableRow(LayoutInflater inflater, ViewGroup container, int colunmNum, boolean hasSaturDay, boolean hasSunDay) {
        List<TableRow> rows = new ArrayList<TableRow>();

        //曜日コラムのセット
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.row_7days, container, false);
        setWeekColumn(tableRow, hasSaturDay, hasSunDay);
        rows.add(tableRow);

        //各コラムの読み込み
        for (int i = 1; i <= colunmNum; i++) {

            String sql = "select * from " + DatabaseHelper.TIME_TABLE_NAME +
                    " where completed = 0 and hour = " + i + ";";


            tableRow = (TableRow) inflater.inflate(R.layout.row_7days, container, false);

            List<TextView> textViews = makeWeekTextView(tableRow, hasSaturDay, hasSunDay, i);

            Cursor cursor = null;
            try {

                cursor = databaseHelper.execRawQuery(sql);

                if (cursor.moveToFirst()) {//データがあるとき
                    do {
                        String day = cursor.getString(cursor.getColumnIndex("day"));
                        String subject = breakString(cursor.getString(cursor.getColumnIndex("subject")));
                        if (AppConstants.MONDAY.equals(day)) {
                            textViews.get(1).setText(subject);
                        } else if (AppConstants.TUESDAYDAY.equals(day)) {
                            textViews.get(2).setText(subject);
                        } else if (AppConstants.WEDNESDAY.equals(day)) {
                            textViews.get(3).setText(subject);
                        } else if (AppConstants.THURSDAY.equals(day)) {
                            textViews.get(4).setText(subject);
                        } else if (AppConstants.FRIDAY.equals(day)) {
                            textViews.get(5).setText(subject);
                        } else if (AppConstants.SATURDAY.equals(day)) {
                            textViews.get(6).setText(subject);
                        } else if (AppConstants.SUNDAY.equals(day)) {
                            if (hasSaturDay){
                                textViews.get(7).setText(subject);
                            }else {
                                textViews.get(6).setText(subject);
                            }

                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            rows.add(tableRow);
        }
        return rows;
    }

    /**
     * ２文字ごとに改行する
     * @return
     */
    private String breakString(String line){
        int lineSize = line.length();
        String newString = "";

        for (int i = 1; i <= lineSize ;i++){
            newString += line.charAt(i-1);
            if(i%2 == 0){
                newString += "\n";
            }
        }
        return newString;
    }

    /**
     * 曜日欄の設定
     *
     * @param tableRow
     * @param hasSat
     * @param hasSun
     */
    private void setWeekColumn(TableRow tableRow, boolean hasSat, boolean hasSun) {

        TextView timeText = (TextView) tableRow.findViewById(R.id.days_time);
        timeText.setText("時限");
        TextView monText = (TextView) tableRow.findViewById(R.id.days_mon);
        monText.setText("月曜");
        TextView tueText = (TextView) tableRow.findViewById(R.id.days_tue);
        tueText.setText("火曜");
        TextView wedText = (TextView) tableRow.findViewById(R.id.days_wed);
        wedText.setText("水曜");
        TextView thurText = (TextView) tableRow.findViewById(R.id.days_thur);
        thurText.setText("木曜");
        TextView friText = (TextView) tableRow.findViewById(R.id.days_fri);
        friText.setText("金曜");
        TextView satText = (TextView) tableRow.findViewById(R.id.days_sat);
        satText.setText("土曜");
        TextView sunText = (TextView) tableRow.findViewById(R.id.days_sun);
        sunText.setText("日曜");

        //いらない列は削除
        if (!hasSat){
            tableRow.removeView(tableRow.findViewById(R.id.days_sat));
        }

        if (!hasSun) {
            tableRow.removeView(tableRow.findViewById(R.id.days_sun));
        }
    }


    /**
     * 各コラムの設定
     *
     * @param tableRow
     * @param hasSat
     * @param hasSun
     * @param time
     * @return
     */
    private List<TextView> makeWeekTextView(TableRow tableRow, boolean hasSat, boolean hasSun, int time) {

        List<TextView> views = new ArrayList<TextView>();

        TextView timeText = (TextView) tableRow.findViewById(R.id.days_time);
        timeText.setText(" " + time + " ");
        TextView monText = (TextView) tableRow.findViewById(R.id.days_mon);
        TextView tueText = (TextView) tableRow.findViewById(R.id.days_tue);
        TextView wedText = (TextView) tableRow.findViewById(R.id.days_wed);
        TextView thurText = (TextView) tableRow.findViewById(R.id.days_thur);
        TextView friText = (TextView) tableRow.findViewById(R.id.days_fri);
        TextView satText = (TextView) tableRow.findViewById(R.id.days_sat);
        TextView sunText = (TextView) tableRow.findViewById(R.id.days_sun);

        views.add(timeText);
        views.add(monText);
        views.add(tueText);
        views.add(wedText);
        views.add(thurText);
        views.add(friText);
        views.add(satText);
        views.add(sunText);

        if (!hasSun) {
            tableRow.removeView(tableRow.findViewById(R.id.days_sat));
        }
        if (!hasSat) {
            tableRow.removeView(tableRow.findViewById(R.id.days_sun));
        }

        return views;
    }


}
