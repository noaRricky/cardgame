package com.zsh.ricky.cardmanager.fragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Thinker on 2017/11/5.
 */

public class HistoryFragment extends Fragment {
    Context context;
    public HistoryFragment(){}
    @SuppressLint("ValidFragment")
    public HistoryFragment(Context ct){
        context=ct;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_historyfragment,null);
        return view;
    }

}
