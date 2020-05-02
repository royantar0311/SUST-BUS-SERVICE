package com.sustbus.driver.adminPanel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sustbus.driver.R;
import com.sustbus.driver.util.UserInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StudentsRecyclerAdapter extends FirestoreRecyclerAdapter<UserInfo, StudentsRecyclerAdapter.StudentsViewHolder> {
    private static final String TAG = "StudentsRecyclerAdapter";
    CheckChangedListener checkChangedListener;

    public StudentsRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserInfo> options, CheckChangedListener checkChangedListener) {
        super(options);
        this.checkChangedListener = checkChangedListener;
    }

    @NonNull
    @Override
    public StudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);
        return new StudentsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull StudentsViewHolder holder, int position, @NonNull UserInfo userInfo) {
        holder.username.setText(userInfo.getUserName());
        holder.regino.setText("#" + userInfo.getRegiNo());
        holder.aSwitch.setChecked(userInfo.isPermitted());
    }


    class StudentsViewHolder extends RecyclerView.ViewHolder {
        TextView username, regino;
        Switch aSwitch;

        public StudentsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.row_item_user_name_tv);
            regino = itemView.findViewById(R.id.row_item_regino_tv);
            aSwitch = itemView.findViewById(R.id.row_item_is_permitted_switch);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: ");
                    checkChangedListener.onItemClicked(getSnapshots().getSnapshot(getAdapterPosition()).getString("uId"));
                }
            });

            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                    checkChangedListener.onSwitchStateChanged(isChecked, getSnapshots().getSnapshot(getAdapterPosition()));
                }
            });
        }
    }

}
