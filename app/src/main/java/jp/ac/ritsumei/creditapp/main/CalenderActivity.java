package jp.ac.ritsumei.creditapp.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import java.util.Locale;
import java.util.TimeZone;

import jp.ac.ritsumei.creditapp.app.R;

public class CalenderActivity extends Activity {

    int apiLevel;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO カレンダーの表示 登録 削除
        setContentView(R.layout.fragment_month);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.month_linewrlayout);

        apiLevel = Build.VERSION.SDK_INT;

        if(apiLevel < 11) {

        }else {
            CalendarView calendarView = new CalendarView(getApplication());
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month,
                                                int dayOfMonth) {
                    // TODO Auto-generated method stub

                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            calendarView.setLayoutParams(params);
            if(16 <= apiLevel){
                calendarView.setFocusedMonthDateColor(Color.BLACK);
                calendarView.setBackgroundColor(Color.LTGRAY);
                calendarView.setSelectedWeekBackgroundColor(Color.GRAY);
                calendarView.setWeekNumberColor(Color.BLACK);
                calendarView.setWeekSeparatorLineColor(Color.BLACK);
            }
            linearLayout.addView(calendarView);
        }
    }


    /**
     * カレンダーにイベントの登録
     * @param context
     * @param title
     * @param startMsec
     * @param endMsec
     */
    public void registGCalendar(Context context, String title, long startMsec, long endMsec){
        if(14 <= apiLevel){
            registGCalendarV14(context,title, startMsec, endMsec);
        }else{
            registGCalendarV13(context,title, startMsec, endMsec);
        }
    }




    // API level 14 (4.0)以上．ICS向け
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registGCalendarV14(Context context, String title, long startMsec, long endMsec) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                        // UTCではなくローカルタイムを設定する
                        // タイムゾーンは，インテントで起動するカレンダー登録画面で吸収しているように見える
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMsec)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMsec)
                .putExtra(CalendarContract.Events.TITLE, title);
        context.startActivity(intent);
    }

    // API level 14 (4.0)以上．ICS向け コンテントプロバイダー方式
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registGCalendarV14Silent(Context context, String title, long startMsec, long endMsec) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.CALENDAR_ID, 1);
        cv.put(CalendarContract.Events.TITLE, title);
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
        cv.put(CalendarContract.Events.DTSTART, convToEnglishTime(startMsec));
        cv.put(CalendarContract.Events.DTEND, convToEnglishTime(endMsec));

        ContentResolver cr = context.getContentResolver();
        cr.insert(CalendarContract.Events.CONTENT_URI, cv);
    }

    // API level 13 (3.2)以下
    public void registGCalendarV13(Context context, String title, long startMsec, long endMsec) {
        ContentValues cv = new ContentValues();
        cv.put("calendar_id", 1);
        cv.put("title", title);
        cv.put("eventTimezone",TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
        cv.put("dtstart", convToEnglishTime(startMsec));
        cv.put("dtend", convToEnglishTime(endMsec));

        ContentResolver cr = context.getContentResolver();
        cr.insert(Uri.parse(Build.VERSION.SDK_INT >= 8 ?
                "content://com.android.calendar/events" :
                "content://calendar/events"), cv);
    }

    /**
     * DateをENGLISHロケールに変換する
     * @param dateMsec
     * @return
     */
    private static long convToEnglishTime(long dateMsec) {
        Time androTime;
        androTime = new Time();
        androTime.switchTimezone(TimeZone.getDefault().getDisplayName(Locale.ENGLISH));
        androTime.set(dateMsec);

        return androTime.normalize(true);
    }
}
