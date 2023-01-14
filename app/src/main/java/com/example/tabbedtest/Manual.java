package com.example.tabbedtest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.tabbedtest.Prevelent.Prevelent;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Manual#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Manual extends Fragment  {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private static MainActivity main ;

    Button updateButton;
    SwitchCompat m1Man , m2Man , m3Man , m4Man;
    String toSend , feedbackMsg;
    SendDataInterface sendDataInterface ;

    public Manual() {
        // Required empty public constructor
    }

    public static Manual newInstance(String param1, String param2) {
        Manual fragment = new Manual();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public interface SendDataInterface {
        void SendData(String data , String feedback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        main = new MainActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.manual, container, false);
        updateButton=  view.findViewById(R.id.updateMan);
        m1Man = view.findViewById(R.id.motor1_man);
        m2Man = view.findViewById(R.id.motor2_man);
        m3Man = view.findViewById(R.id.motor3_man);
        m4Man = view.findViewById(R.id.motor4_man);

        // set onclick listener on buttons
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m1Man.isChecked() && m2Man.isChecked() && m3Man.isChecked() && m4Man.isChecked())
                    feedbackMsg=getString(R.string.all_motors_are_turned_on) ;
                else if(!m1Man.isChecked() && !m2Man.isChecked() && !m3Man.isChecked() && !m4Man.isChecked())
                    feedbackMsg=getString(R.string.all_motors_are_turned_off) ;
                else  feedbackMsg = (m1Man.isChecked() ? getString(R.string.motor_1) : " " )+(m2Man.isChecked() ? getString(R.string.motor_2) : " " )+
                            (m3Man.isChecked() ? getString(R.string.motor_3) : " " )+(m4Man.isChecked() ? getString(R.string.motor_4) : " " ) + getString(R.string.are_turned_on);
                // perform any action on button click
                toSend="M"+(m1Man.isChecked() ? "1" : "0" )+(m2Man.isChecked() ? "1" : "0" )+(m3Man.isChecked() ? "1" : "0" )+(m4Man.isChecked() ? "1" : "0" );

                sendDataInterface.SendData(toSend , feedbackMsg);
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            sendDataInterface= (SendDataInterface) activity;
        }catch (RuntimeException e){
            throw new RuntimeException(activity.toString()+" Must implement method");
        }
    }

}