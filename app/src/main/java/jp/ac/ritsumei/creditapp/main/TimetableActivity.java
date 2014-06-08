package jp.ac.ritsumei.creditapp.main;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.TextView;

import jp.ac.ritsumei.creditapp.app.R;

public class TimetableActivity extends Activity {

    private Time currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timetable);

        //日時設定
        setCurrentTime();

        //TODO データベースから読み込み 曜日


        //TODO 週


        //TODO 月


    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    protected void setCurrentTime() {
        currentTime = new Time("asia/Tokyo");
        currentTime.setToNow();
        TextView timeText = (TextView) findViewById(R.id.extView1);
        timeText.setText((currentTime.month + 1) + "月" + currentTime.monthDay + "日");
    }



}
