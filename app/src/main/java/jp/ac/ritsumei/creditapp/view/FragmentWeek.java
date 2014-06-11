package jp.ac.ritsumei.creditapp.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.ac.ritsumei.creditapp.app.R;

/**
 * Created by miyazakikazuya on 2014/06/11.
 */
public class FragmentWeek extends Fragment{

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week, container, false);
    }
}
