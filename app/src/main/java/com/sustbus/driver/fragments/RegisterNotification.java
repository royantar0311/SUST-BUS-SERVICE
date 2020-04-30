package com.sustbus.driver.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;
import com.sustbus.driver.R;
import com.sustbus.driver.util.CallBack;
import com.sustbus.driver.util.MapUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.fragment.app.Fragment;

public class RegisterNotification extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private ChipsInput chipsInput;
    private List<PlaceChips> chipsList;

    private CheckBox away,towards,_6to9,_9to12,_12to15,_15to18,_18to22,wholeday;

    private CallBack callBack;
    public RegisterNotification(CallBack callBack){
        this.callBack=callBack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_register_notification, container, false);
        chipsInput=v.findViewById(R.id.chips_for_registration);
        away=v.findViewById(R.id.away_checkbox);
        towards=v.findViewById(R.id.towards_checkbox);
        _6to9=v.findViewById(R.id._6to9);
        _9to12=v.findViewById(R.id._9to12);
        _12to15=v.findViewById(R.id._12to15);
        _15to18=v.findViewById(R.id._15to18);
        _18to22=v.findViewById(R.id._18to22);
        wholeday=v.findViewById(R.id._whole_day);
        v.findViewById(R.id.notification_register_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        ((TextView)getActivity().findViewById(R.id.notification_setting_appbar_tv)).setText("Setup Registration");


        _18to22.setOnCheckedChangeListener(this);
        _15to18.setOnCheckedChangeListener(this);
        _12to15.setOnCheckedChangeListener(this);
        _9to12.setOnCheckedChangeListener(this);
        _6to9.setOnCheckedChangeListener(this);
        wholeday.setOnCheckedChangeListener(this);

        chipsInput.setMaxRows(20);
        chipsList=new ArrayList<>();

        for(String s: MapUtil.placeList){
            chipsList.add(new PlaceChips(s));
        }

        chipsInput.setFilterableList(chipsList);
        return  v;


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().findViewById(R.id.notification_setting_fab).setVisibility(View.VISIBLE);
        chipsInput.addChip(chipsList.get(0));
        ((TextView)getActivity().findViewById(R.id.notification_setting_appbar_tv)).setText("Notification Rules");
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked && buttonView.getId()==R.id._whole_day){
            _18to22.setChecked(false);
            _15to18.setChecked(false);
            _12to15.setChecked(false);
            _9to12.setChecked(false);
            _6to9.setChecked(false);

        }
        else if(isChecked){
            wholeday.setChecked(false);
        }

    }
    public void register(){

        List<PlaceChips> selectedPlaces;
        selectedPlaces= (List<PlaceChips>) chipsInput.getSelectedChipList();
        if(selectedPlaces.size()==0){
            Snackbar.make(chipsInput,"Please Enter at least one valid place",4000).show();
            return;
        }
        if(!away.isChecked() && !towards.isChecked()){
            Snackbar.make(chipsInput,"Please Choose at least one rule",4000).show();
            return;
        }

        if(!_18to22.isChecked() && !_12to15.isChecked() && !_6to9.isChecked() && !_9to12.isChecked() && !_15to18.isChecked() && !wholeday.isChecked() ){
            Snackbar.make(chipsInput,"Please Choose at least one time",4000).show();
            return;
        }



        List<String> tokenList=new ArrayList<>();

        for(int i=0;i<selectedPlaces.size();i++){

            String name=MapUtil.removeSpace(selectedPlaces.get(i).getInfo());

            if(away.isChecked()){
                String topic=name+".away.";

                if(wholeday.isChecked()){
                    tokenList.add(topic+"00_00");
                }
                else {
                    if(_6to9.isChecked())tokenList.add(topic+"06_09");
                    if(_9to12.isChecked())tokenList.add(topic+"09_12");
                    if(_12to15.isChecked())tokenList.add(topic+"12_15");
                    if(_15to18.isChecked())tokenList.add(topic+"15_18");
                    if(_18to22.isChecked())tokenList.add(topic+"18_22");
                }
            }

            if(towards.isChecked()){
                String topic=name+".towards.";

                if(wholeday.isChecked()){
                    tokenList.add(topic+"00_00");
                }
                else {
                    if(_6to9.isChecked())tokenList.add(topic+"06_09");
                    if(_9to12.isChecked())tokenList.add(topic+"09_12");
                    if(_12to15.isChecked())tokenList.add(topic+"12_15");
                    if(_15to18.isChecked())tokenList.add(topic+"15_18");
                    if(_18to22.isChecked())tokenList.add(topic+"18_22");
                }
            }
        }
        SharedPreferences sharedPreferences=getActivity().getSharedPreferences("NOTIFICATIONS", Context.MODE_PRIVATE);
        Set<String> keySet=sharedPreferences.getStringSet("tokenSet",new HashSet<>());

        Set<String> tmp=new HashSet<>(keySet);

        for(int i=0;i<tokenList.size();i++){
            if(!keySet.contains(tokenList.get(i))) {
                FirebaseMessaging.getInstance().subscribeToTopic(tokenList.get(i));
                tmp.add(tokenList.get(i));
                //Log.d("DEBMES","added "+tokenList.get(i));
            }
            //Log.d("DEBMES",tokenList.get(i));
        }


        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putStringSet("tokenSet",tmp);
        editor.commit();


        callBack.ok();
        getActivity().onBackPressed();

    }

    class PlaceChips implements ChipInterface {

        String place;
        PlaceChips(String name){
            place=name;
        }
        @Override
        public Object getId() {
            return null;
        }

        @Override
        public Uri getAvatarUri() {
            return null;
        }

        @Override
        public Drawable getAvatarDrawable() {
            return null;
        }

        @Override
        public String getLabel() {
            return place;
        }

        @Override
        public String getInfo() {
            return place;
        }
    }

}
