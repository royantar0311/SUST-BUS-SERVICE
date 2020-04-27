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

public class CustomQueryRecyclerAdapter extends FirestoreRecyclerAdapter<UserInfo, CustomQueryRecyclerAdapter.CustomQueryRecyclerViewHolder>{
    private static final String TAG = "CustomQueryRecyclerAdap";
    CheckChangedListener checkChangedListener;
    public CustomQueryRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserInfo> options,CheckChangedListener checkChangedListener) {
        super(options);
        Log.d(TAG, "CustomQueryRecyclerAdapter: ");
        this.checkChangedListener = checkChangedListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull CustomQueryRecyclerViewHolder holder, int position, @NonNull UserInfo userInfo) {
        Log.d(TAG, "onBindViewHolder: ");
        holder.regino.setText(userInfo.getRegiNo());
        holder.username.setText(userInfo.getUserName());
        holder.aSwitch.setChecked(userInfo.getIsStudentPermitted()==1?true:false);
    }

    @NonNull
    @Override
    public CustomQueryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_item,parent,false);
        return new CustomQueryRecyclerViewHolder(view);
    }

    class CustomQueryRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView username,regino;
        Switch aSwitch;
        public CustomQueryRecyclerViewHolder(View itemView){
            super(itemView);
            Log.d(TAG, "CustomQueryRecyclerViewHolder: ");
            username = itemView.findViewById(R.id.row_item_user_name_tv);
            regino = itemView.findViewById(R.id.row_item_regino_tv);
            aSwitch = itemView.findViewById(R.id.row_item_is_permitted_switch);
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: " + isChecked);
                    checkChangedListener.onSwitchStateChanged(isChecked,getSnapshots().getSnapshot(getAdapterPosition()));
                }
            });
        }
    }
    interface CheckChangedListener{
        public void onSwitchStateChanged(boolean isChecked, DocumentSnapshot snapshot);
    }
}
