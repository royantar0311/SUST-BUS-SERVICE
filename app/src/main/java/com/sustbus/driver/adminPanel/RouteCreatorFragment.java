package com.sustbus.driver.adminPanel;

import android.app.TimePickerDialog;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;
import com.pchmn.materialchips.views.ChipsInputEditText;
import com.pchmn.materialchips.views.FilterableListView;
import com.sustbus.driver.R;
import com.sustbus.driver.util.MapUtil;
import com.sustbus.driver.util.RouteInformation;

import java.util.ArrayList;
import java.util.List;


public class RouteCreatorFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, CompoundButton.OnCheckedChangeListener {
    RouteInformation routeInformation;
    private ChipsInput chipsInput;
    private Button submitButton,timeChooserButton;
    private TextView selectedTimetv;
    private CheckBox comesBackCheckbox,addRoutedetails;
    private EditText fromChip,toChip;
    private EditText showInfoEditText;
    private List<String> places;
    private List<PlaceChips> placeChips;
    private TimePickerDialog timePickerDialog;
    View root;

    RouteCreatorFragment(){
      places= MapUtil.placeList;
      placeChips=new ArrayList<>();

      for (String s:places){
          PlaceChips tmp=new PlaceChips(s);
          placeChips.add(tmp);
      }
      for (String s:places){
          PlaceChips tmp=new PlaceChips(new String(" "+s));
          placeChips.add(tmp);
      }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(root!=null)return root;
        root=inflater.inflate(R.layout.fragment_route_creator, container, false);

        chipsInput=root.findViewById(R.id.route_chips);
        chipsInput.setFilterableList(placeChips);
        showInfoEditText=root.findViewById(R.id.information_to_edittext);
        timeChooserButton=root.findViewById(R.id.time_chooser);
        selectedTimetv=root.findViewById(R.id.chosen_time);
        fromChip=root.findViewById(R.id.from_chip);
        toChip=root.findViewById(R.id.to_chip);
        comesBackCheckbox=root.findViewById(R.id.comes_back);
        submitButton=root.findViewById(R.id.submit_route);
        addRoutedetails=root.findViewById(R.id.add_route_details);
        timePickerDialog=new TimePickerDialog(getContext(),this,12,45,false);
        timeChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmit();
            }
        });
        comesBackCheckbox.setOnCheckedChangeListener(this);
        addRoutedetails.setOnCheckedChangeListener(this);
        return root;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String time=new String();
        String ampm=null;
        if(hourOfDay<12){
            if(hourOfDay==0){
                time+="12";
            }
            else {
                time+=(hourOfDay<10?"0":"")+hourOfDay;
            }
            ampm=" am";
        }
        else {
            ampm=" pm";

            if(hourOfDay!=12)hourOfDay%=12;
            time+=(hourOfDay<10?"0":"")+hourOfDay;

        }

        time+=":"+(minute<10?"0":"")+minute+ampm;

        selectedTimetv.setText(time);
        timeChooserButton.setText("Choose again");
    }

    public void handleSubmit(){





    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        List<PlaceChips>selected=new ArrayList<>((List<PlaceChips>)chipsInput.getSelectedChipList());

        if(buttonView.getId()==R.id.add_route_details){
            if(isChecked){

             String txt=new String();

             for(int i=0;i<selected.size();i++){

              if(i==0)txt+=selected.get(i).getInfo();
              else txt+="-"+selected.get(i).getInfo();
             }

             showInfoEditText.setText(txt);
            }
            return;
        }

        if(isChecked){
            int len=selected.size();

            for (int i=len-2;i>=0;i--){
                String tmp=" "+selected.get(i).getInfo().trim();
                if(tmp.equals(selected.get(i).getInfo()))tmp=tmp.trim();

                chipsInput.addChip(new PlaceChips(tmp));
            }
        }


    }
}

class PlaceChips implements ChipInterface {

    String place;
    Integer id;

    PlaceChips(String name){
        place=name;
    }
    @Override
    public Object getId() {
        return id;
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


