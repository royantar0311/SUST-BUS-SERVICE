package com.sustbus.driver.adminPanel;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;
import com.sustbus.driver.R;
import com.sustbus.driver.util.MapUtil;
import com.sustbus.driver.util.RouteInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class RouteCreatorFragment extends Fragment implements TimePickerDialog.OnTimeSetListener, CompoundButton.OnCheckedChangeListener {
    public RouteInformation routeInformation;
    View root;
    private ChipsInput chipsInput;
    private Button submitButton, timeChooserButton;
    private TextView selectedTimetv;
    private CheckBox addRoutedetails;
    private EditText fromChip, toChip;
    private EditText showInfoEditText;
    private List<String> places;
    private List<PlaceChips> placeChips;
    private TimePickerDialog timePickerDialog;
    private CheckBox studentCb,staffCb,teacherCb;

    RouteCreatorFragment() {
        places = MapUtil.placeList;
        placeChips = new ArrayList<>();

        int id = 0;
        for (String s : places) {
            PlaceChips tmp = new PlaceChips(s);
            placeChips.add(tmp);
        }

        for (String s : places) {
            PlaceChips tmp = new PlaceChips(s + " ");
            placeChips.add(tmp);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root != null) return root;
        root = inflater.inflate(R.layout.fragment_route_creator, container, false);

        chipsInput = root.findViewById(R.id.route_chips);
        chipsInput.setFilterableList(placeChips);
        showInfoEditText = root.findViewById(R.id.information_to_edittext);
        timeChooserButton = root.findViewById(R.id.time_chooser);
        selectedTimetv = root.findViewById(R.id.chosen_time);
        fromChip = root.findViewById(R.id.from_chip);
        toChip = root.findViewById(R.id.to_chip);
        submitButton = root.findViewById(R.id.submit_route);
        addRoutedetails = root.findViewById(R.id.add_route_details);
        staffCb=root.findViewById(R.id.routeCreator_staff_cb);
        studentCb=root.findViewById(R.id.routeCreator_student_cb);
        teacherCb=root.findViewById(R.id.routeCreator_techer_cb);
        studentCb.setChecked(true);
        timePickerDialog = new TimePickerDialog(getContext(), this, 12, 45, false);
        timeChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });
        selectedTimetv.setOnClickListener(new View.OnClickListener() {
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
        studentCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    staffCb.setChecked(false);
                    teacherCb.setChecked(false);
                }
            }
        });
        staffCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    studentCb.setChecked(false);
                    teacherCb.setChecked(false);
                }
            }
        });

        teacherCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    studentCb.setChecked(false);
                    staffCb.setChecked(false);
                }
            }
        });

        addRoutedetails.setOnCheckedChangeListener(this);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((TextView) getActivity().findViewById(R.id.rm_appbar_tv)).setText("Route Creator");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String time = new String();
        String ampm = null;
        if (hourOfDay < 12) {
            if (hourOfDay == 0) {
                time += "12";
            } else {
                time += (hourOfDay < 10 ? "0" : "") + hourOfDay;
            }
            ampm = " am";
        } else {
            ampm = " pm";

            if (hourOfDay != 12) hourOfDay %= 12;
            time += (hourOfDay < 10 ? "0" : "") + hourOfDay;

        }

        time += ":" + (minute < 10 ? "0" : "") + minute + ampm;

        selectedTimetv.setText(time);
        timeChooserButton.setText("Choose again");
    }

    public void handleSubmit() {
        submitButton.setClickable(false);
        List<PlaceChips> selected = (List<PlaceChips>) chipsInput.getSelectedChipList();

        if (selected.size() < 2 || selectedTimetv.getText().toString().trim().equals("_ _ : _ _ am")) {
            Snackbar.make(submitButton, "Enter Route and time properly", 3000).show();
            submitButton.setClickable(true);
            return;
        }

        String path = new String();

        for (PlaceChips tmp : selected) {
            path += tmp.getInfo() + ";";
        }
        String title = fromChip.getText().toString().trim() + "-" + toChip.getText().toString().trim();
        Map<String, String> data = new HashMap<>();
        data.put("path", path);
        data.put("title", title);
        data.put("time", selectedTimetv.getText().toString().trim());
        data.put("show", showInfoEditText.getText().toString().trim());
        data.put("for", studentCb.isChecked()?"s":(teacherCb.isChecked()?"t":"sf"));

        ProgressDialog dialog = new ProgressDialog(getContext());
        final String tmpp = path;
        final String tmpt = title;
        dialog.setMessage("uploading..");
        dialog.show();
        FirebaseFirestore.getInstance().collection("routes").add(data)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Added", Toast.LENGTH_LONG).show();
                            getActivity().getIntent().putExtra("added", true);
                            getActivity().getIntent().putExtra("time", selectedTimetv.getText().toString().trim());
                            getActivity().getIntent().putExtra("path", tmpp);
                            getActivity().getIntent().putExtra("show", showInfoEditText.getText().toString().trim());
                            getActivity().getIntent().putExtra("title", tmpt);
                            getActivity().getIntent().putExtra("id", task.getResult().getId());
                            getActivity().getIntent().putExtra("for", studentCb.isChecked()?"s":(teacherCb.isChecked()?"t":"sf"));
                            getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getContext(), "Check Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        submitButton.setClickable(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            List<PlaceChips> selected = new ArrayList<>((List<PlaceChips>) chipsInput.getSelectedChipList());

            String txt = new String();

            for (int i = 0; i < selected.size(); i++) {

                if (i == 0) txt += selected.get(i).getInfo();
                else txt += "-" + selected.get(i).getInfo();
            }

            showInfoEditText.append(txt);
        }
        return;
    }

}

class PlaceChips implements ChipInterface {

    String place;


    PlaceChips(String name) {
        place = name;
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
        return place.trim();
    }
}
