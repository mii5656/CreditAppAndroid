package jp.ac.ritsumei.creditapp.main;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.ArrayList;

import jp.ac.ritsumei.creditapp.app.R;
import jp.ac.ritsumei.creditapp.sqlite.DatabaseHelper;
import jp.ac.ritsumei.creditapp.util.AppConstants;
import jp.ac.ritsumei.creditapp.view.FragmentDays;
import jp.ac.ritsumei.creditapp.view.FragmentMonth;
import jp.ac.ritsumei.creditapp.view.FragmentWeek;

public class TimetableActivity extends ActionBarActivity {

    private Time currentTime;

    private DatabaseHelper databaseHelper;

    private ActionBar mActionBar;

    private ArrayList<Fragment> mTabFragments;

    private boolean hasSaturDay;
    private boolean hasSunDay;
    private int columnNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasSaturDay = false;
        hasSunDay = false;

        mTabFragments = new ArrayList<Fragment>();

        //データベースヘルパーの取得
        databaseHelper = new DatabaseHelper(getApplication());


        mActionBar = getSupportActionBar();

        //レイアウトの設定
        setContentView(R.layout.timetable);


        //曜日タブの設定
        setActionBar();

        //現在日時設定
        setCurrentTime();

    }


    /**
     * 現在時刻の取得
     */
    protected void setCurrentTime() {
        currentTime = new Time("asia/Tokyo");
        currentTime.setToNow();
        mActionBar.setTitle(getString(R.string.app_name) + "   " + (currentTime.month + 1) + "/" + currentTime.monthDay);
    }


    protected void checkCurriculumInfo() {
        Cursor cursor = null;

        try {
            //土日があるか確認
            cursor = databaseHelper.execRawQuery("select distinct day from "
                    + DatabaseHelper.TIME_TABLE_NAME + " where completed = 0 ;");

            if (cursor.moveToFirst()) {
                do {
                    String day = cursor.getString(cursor.getColumnIndex("day"));
                    if (AppConstants.SATURDAY.equals(day)) {
                        hasSaturDay = true;
                    } else if (AppConstants.SUNDAY.equals(day)) {
                        hasSunDay = true;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Cursor cursor2 = null;
        //コマ数の確認
        try {
            cursor2 = databaseHelper.execRawQuery("select max (hour) from "
                    + DatabaseHelper.TIME_TABLE_NAME + " where completed = 0 ;");

            if (cursor2.moveToFirst()) {
                columnNum = cursor2.getInt(0);
                if (columnNum < 5) {
                    columnNum = 5;
                }
            } else {
                columnNum = 5;//適当
            }
            cursor2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 曜日タブの設定
     */
    protected void setActionBar() {

        checkCurriculumInfo();

        MainTabListener listener = new MainTabListener(this);

        mTabFragments.add(FragmentWeek.newInstance(columnNum,hasSaturDay,hasSunDay,databaseHelper));

        mTabFragments.add(FragmentDays.newInstance(AppConstants.MONDAY, columnNum, databaseHelper));
        mTabFragments.add(FragmentDays.newInstance(AppConstants.TUESDAYDAY, columnNum, databaseHelper));
        mTabFragments.add(FragmentDays.newInstance(AppConstants.WEDNESDAY, columnNum, databaseHelper));
        mTabFragments.add(FragmentDays.newInstance(AppConstants.THURSDAY, columnNum, databaseHelper));
        mTabFragments.add(FragmentDays.newInstance(AppConstants.FRIDAY, columnNum, databaseHelper));

        mTabFragments.add(new FragmentMonth());

        mActionBar.addTab(mActionBar.newTab().setText("週").setTabListener(listener));
        mActionBar.addTab(mActionBar.newTab().setText("月").setTabListener(listener));
        mActionBar.addTab(mActionBar.newTab().setText("火").setTabListener(listener));
        mActionBar.addTab(mActionBar.newTab().setText("水").setTabListener(listener));
        mActionBar.addTab(mActionBar.newTab().setText("木").setTabListener(listener));
        mActionBar.addTab(mActionBar.newTab().setText("金").setTabListener(listener));

        if (hasSaturDay) {
            mTabFragments.add(FragmentDays.newInstance(AppConstants.SATURDAY, columnNum, databaseHelper));
            mActionBar.addTab(mActionBar.newTab().setText("土").setTabListener(listener));
        }

        if (hasSunDay) {
            mTabFragments.add(FragmentDays.newInstance(AppConstants.SUNDAY, columnNum, databaseHelper));
            mActionBar.addTab(mActionBar.newTab().setText("日").setTabListener(listener));
        }

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューの要素を追加して取得
        MenuItem settingItem = menu.add(AppConstants.CALENDAR);
        // アイコンを設定
        settingItem.setIcon(android.R.drawable.ic_menu_month);
        //常に表示
        MenuItemCompat.setShowAsAction(settingItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add(AppConstants.SETTING);
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_preferences);
        MenuItemCompat.setShowAsAction(settingItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }


    //TODO viewの切替
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AppConstants.CALENDAR.equals(item.getTitle())) {
            //startActivity(new Intent(TimetableActivity.this, CalenderActivity.class));

            Intent i = new Intent();

            if(8 < Build.VERSION.SDK_INT){
                i.setClassName("com.google.android.calendar","com.android.calendar.LaunchActivity");
            }else{
                i.setClassName("com.android.calendar","com.android.calendar.LaunchActivity");
            }
            startActivity(i);
        } else if (AppConstants.SETTING.equals(item.getTitle())) {

        }
        return true;
    }


    /*
     * ActionBarのタブリスナー
     */
    public class MainTabListener implements ActionBar.TabListener {

        private final Activity activity;

        public MainTabListener(Activity activity) {
            this.activity = activity;
        }

        /*
         * 選択されているタブが再度選択された場合に実行
         *
         * @see
         * android.support.v7.app.ActionBar.TabListener#onTabReselected(android.
         * support.v7.app.ActionBar.Tab,
         * android.support.v4.app.FragmentTransaction)
         */
        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub
//            Log.i("MainActivity", "onTabReselected " + tab.getText()
//                    + " : position => " + tab.getPosition());
        }

        /*
         * タブが選択された場合に実行
         *
         * @see
         * android.support.v7.app.ActionBar.TabListener#onTabSelected(android
         * .support .v7.app.ActionBar.Tab,
         * android.support.v4.app.FragmentTransaction)
         */
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub
//            Log.i("MainActivity", "onTabSelected " + tab.getText()
//                    + " : position => " + tab.getPosition());
            // Fragmentの置換
            ft.replace(R.id.timetable_contents, mTabFragments.get(tab.getPosition()));
        }

        /*
         * タブの選択が外れた場合に実行
         *
         * @see
         * android.support.v7.app.ActionBar.TabListener#onTabUnselected(android.
         * support.v7.app.ActionBar.Tab,
         * android.support.v4.app.FragmentTransaction)
         */
        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub
//            Log.i("MainActivity", "onTabUnselected " + tab.getText()
//                    + " : position => " + tab.getPosition());

            // Fragment削除
            // ft.remove(mFragment);

        }
    }

}
