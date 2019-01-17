package com.hyj.privacy_protectedcrowdsensingsystem.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hyj.privacy_protectedcrowdsensingsystem.R;

import static android.content.Context.MODE_PRIVATE;


public class UserCenterFragment extends Fragment {

    private TextView titleTextView;
    private TextView taskInfoTextView;
    private Button btnAbandon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        taskInfoTextView = (TextView) view.findViewById(R.id.taskTextView);
        btnAbandon = (Button) view.findViewById(R.id.abandon);
        btnAbandon.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("用户中心");
        SharedPreferences share = getActivity().getSharedPreferences("", MODE_PRIVATE);
        if (share.getFloat("latitude", 0) == 0) {
            titleTextView.setText(" 暂时没有正在执行中的任务");
            taskInfoTextView.setText(" ");
        } else {
            titleTextView.setText(" 正在执行中的任务");
            String taskInfo = share.getString("taskInfo", " 任务地址 :\n 维度区间：\n 经度区间：\n 任务距离：\n 任务内容:");
            taskInfoTextView.setText(taskInfo);
            btnAbandon.setVisibility(View.VISIBLE);
        }

        btnAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences share = getActivity().getSharedPreferences("", MODE_PRIVATE);
                SharedPreferences.Editor editor = share.edit();
                editor.putFloat("latitude", 0);
                editor.putFloat("longitude", 0);
                editor.apply();
                titleTextView.setText(" 暂时没有正在执行中的任务");
                taskInfoTextView.setText(" ");
                btnAbandon.setVisibility(View.INVISIBLE);
            }
        });
    }
}
