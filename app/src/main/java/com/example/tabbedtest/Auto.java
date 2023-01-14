package com.example.tabbedtest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.tabbedtest.Prevelent.Prevelent;

public class Auto extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    SendDataInterfaceAuto sendDataInterfaceAuto;

    public Auto() {
        // Required empty public constructor
    }

    Button updateButton , sunday , monday , tuesday , wednesday , thursday , friday , saturday;
    SwitchCompat motor1Switch , motor2Switch , motor3Switch , motor4Switch ;
    TimePicker startTimePicker,stopTimePicker;
    int startHourPick , startMinutePick, stopHourPick ,stopMinutePick;
    String StartHour , StartMin , StopHour , StopMin , toSend , feedback;

    public static Auto newInstance(String param1, String param2) {
        Auto fragment = new Auto();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public interface SendDataInterfaceAuto {
        void SendDataAuto(String data, String feedback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.auto, container, false);

        updateButton = view.findViewById(R.id.autoUpdate);
        sunday = view.findViewById(R.id.sunday);
        monday = view.findViewById(R.id.monday);
        tuesday = view.findViewById(R.id.tuesday);
        wednesday = view.findViewById(R.id.wednesday);
        thursday = view.findViewById(R.id.thursday);
        friday = view.findViewById(R.id.friday);
        saturday = view.findViewById(R.id.saturday);

        if (Prevelent.bool){
            DoSaturday();
            DoSunday();
            DoMonday();
            DoTuesday();
            DoWednesday();
            DoThursday();
            DoFriday();
        }else {
            Toast.makeText(getActivity(), R.string.connect_first,Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    void DoSaturday(){
        saturday.setOnClickListener(v -> ShowPopup("1000000", getString(R.string.saturday) , 0));
    }
    void DoSunday(){
        sunday.setOnClickListener(v -> ShowPopup("0100000" , getString(R.string.sunday),1));
    }
    void DoMonday(){
        monday.setOnClickListener(v -> ShowPopup("0010000",getString(R.string.monday),2));
    }
    void DoTuesday(){
        tuesday.setOnClickListener(v -> ShowPopup("0001000",getString(R.string.tuesday),3));
    }
    void DoWednesday(){
        wednesday.setOnClickListener(v -> ShowPopup("0000100",getString(R.string.wednesday),4));
    }
    void DoThursday(){
        thursday.setOnClickListener(v -> ShowPopup("0000010",getString(R.string.thursday),5));
    }
    void DoFriday(){
        friday.setOnClickListener(v -> ShowPopup("0000001",getString(R.string.friday),6));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            sendDataInterfaceAuto= (Auto.SendDataInterfaceAuto) activity;
        }catch (RuntimeException e){
            throw new RuntimeException(activity.toString()+" Must implement method");
        }
    }

    public void ShowPopup(String day , String d , int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.days_dialog, null);

        builder.setView(view);
        builder.setTitle(d);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= 23 ){
                startHourPick = startTimePicker.getHour();
                startMinutePick = startTimePicker.getMinute();
                stopHourPick= stopTimePicker.getHour();
                stopMinutePick= stopTimePicker.getMinute();
                if (startHourPick <10) StartHour="0"+startHourPick;
                else StartHour=""+startHourPick;
                if (startMinutePick <10) StartMin="0"+startMinutePick;
                else StartMin=""+startMinutePick;
                if (stopHourPick <10) StopHour="0"+stopHourPick;
                else StopHour=""+stopHourPick;
                if (stopMinutePick <10) StopMin="0"+stopMinutePick;
                else StopMin=""+stopMinutePick;
            }
            else{
                startHourPick = startTimePicker.getCurrentHour();
                startMinutePick = startTimePicker.getCurrentMinute();
                stopHourPick =stopTimePicker.getCurrentHour();
                stopMinutePick =stopTimePicker.getCurrentMinute();
                if (startHourPick <10) StartHour="0"+startHourPick;
                else StartHour=""+startHourPick;
                if (startMinutePick <10) StartMin="0"+startMinutePick;
                else StartMin=""+startMinutePick;
                if (stopHourPick <10) StopHour="0"+stopHourPick;
                else StopHour=""+stopHourPick;
                if (stopMinutePick <10) StopMin="0"+stopMinutePick;
                else StopMin=""+stopMinutePick;
            }
            toSend= "A"+(motor1Switch.isChecked() ? "1" : "0" )+(motor2Switch.isChecked() ? "1" : "0" )+(motor3Switch.isChecked() ? "1" : "0" )
                    +(motor4Switch.isChecked() ? "1" : "0" )+StartHour+StartMin+StopHour+StopMin+day;
            if (!motor1Switch.isChecked() && !motor2Switch.isChecked() && !motor3Switch.isChecked() && !motor4Switch.isChecked())
                feedback= getString(R.string.done);
            else
                feedback =(motor1Switch.isChecked() ? getString(R.string.motor_1) : " " )+(motor2Switch.isChecked() ? getString(R.string.motor_2) : " " )+
                    (motor3Switch.isChecked() ? getString(R.string.motor_3): " " )+(motor4Switch.isChecked() ? getString(R.string.motor_4) : " " ) + getString(R.string.will_be_turned_on)
                    + StartHour+":" +StartMin+getString(R.string.to)+StopHour+":"+StopMin +"\n"+ getString(R.string.on) + d;
            //Toast.makeText(getActivity(),toSend,Toast.LENGTH_SHORT).show();
            String motorState = (motor1Switch.isChecked() ? "1" : "0" )+(motor2Switch.isChecked() ? "1" : "0" )+(motor3Switch.isChecked() ? "1" : "0" )
                    +(motor4Switch.isChecked() ? "1" : "0" ) ;
            String strtT = StartHour+StartMin ;
            String stpT = StopHour+StopMin ;
            Prevelent.stopTimee[index]= stpT;
            Prevelent.startTimee[index]=strtT;
            Prevelent.statee[index]=motorState;
            sendDataInterfaceAuto.SendDataAuto(toSend,feedback);
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        motor1Switch = view.findViewById(R.id.motor1_switch);
        motor2Switch = view.findViewById(R.id.motor2_switch);
        motor3Switch = view.findViewById(R.id.motor3_switch);
        motor4Switch = view.findViewById(R.id.motor4_switch);
        startTimePicker = view.findViewById(R.id.time_picker_start);
        startTimePicker.setIs24HourView(true);
        stopTimePicker = view.findViewById(R.id.time_picker_stop);
        stopTimePicker.setIs24HourView(true);

        String StartT=Prevelent.startTimee[index];
        String StopT=Prevelent.stopTimee[index];
        String motorS=Prevelent.statee[index];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int StartHour = Integer.parseInt(StartT.substring(0,2));
            int StartMin = Integer.parseInt(StartT.substring(2,4));
            int StopHour = Integer.parseInt(StopT.substring(0,2));
            int StopMin = Integer.parseInt(StopT.substring(2,4));
            String S1 = motorS.substring(0,1);
            String S2 = motorS.substring(1,2);
            String S3 = motorS.substring(2,3);
            String S4 = motorS.substring(3,4);

            startTimePicker.setHour(StartHour);
            startTimePicker.setMinute(StartMin);
            stopTimePicker.setHour(StopHour);
            stopTimePicker.setMinute(StopMin);

            motor1Switch.setChecked(S1.equals("1"));
            motor2Switch.setChecked(S2.equals("1"));
            motor3Switch.setChecked(S3.equals("1"));
            motor4Switch.setChecked(S4.equals("1"));

        }
        builder.show();
    }

}