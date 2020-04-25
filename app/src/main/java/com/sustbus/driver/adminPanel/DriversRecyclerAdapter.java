package com.sustbus.driver.adminPanel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sustbus.driver.R;
import com.sustbus.driver.util.UserInfo;

public class DriversRecyclerAdapter extends FirestoreRecyclerAdapter<UserInfo,DriversRecyclerAdapter.DriversViewHolder> {
    private static final String TAG = "DriversRecyclerAdapter";
    CheckChangedListener checkChangedListener;

    public DriversRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserInfo> options,CheckChangedListener checkChangedListener) {
        super(options);
        Log.d(TAG, "DriversRecyclerAdapter: ");
        this.checkChangedListener = checkChangedListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull DriversViewHolder holder, int position, @NonNull UserInfo userInfo) {
        Log.d(TAG, "onBindViewHolder: ");
        holder.username.setText(userInfo.getUserName());
        holder.regino.setText("#" + userInfo.getRegiNo());
        holder.aSwitch.setChecked((userInfo.getIsStudentPermitted()!=1)?false:true);
    }

    @NonNull
    @Override
    public DriversViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item,parent,false);
        return new DriversRecyclerAdapter.DriversViewHolder(view);
    }

    class DriversViewHolder extends RecyclerView.ViewHolder{
        TextView username,regino;
        Switch aSwitch;
        public DriversViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "DriversViewHolder: ");
            username = itemView.findViewById(R.id.row_item_user_name_tv);
            regino = itemView.findViewById(R.id.row_item_regino_tv);
            aSwitch = itemView.findViewById(R.id.row_item_is_permitted_switch);

            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                }
            });
        }
    }
    interface  CheckChangedListener{
    }
}
