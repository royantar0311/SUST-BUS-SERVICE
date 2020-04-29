package com.sustbus.driver.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.sustbus.driver.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter2 extends RecyclerView.Adapter<RecyclerViewAdapter2.ViewHolder2>  {

    private Context context;
    private List<String> tokenList;

    public RecyclerViewAdapter2(Context context, List<String> tokenList ) {
        this.context = context;
        this.tokenList=tokenList;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter2.ViewHolder2 onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_notification, viewGroup, false);
        return new RecyclerViewAdapter2.ViewHolder2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter2.ViewHolder2 viewHolder, int position) {

        String s=tokenList.get(position);
        String place= "",rule= "",time= "";

        int i=0;
        for (;;i++){
            if(s.charAt(i)=='.')break;
            if(s.charAt(i)!='_')place+=s.charAt(i);
            else place+=' ';
        }
        int last=++i;

        for (;;i++)if(s.charAt(i)=='.')break;
        rule=s.substring(last,i);
        i++;

        if(rule.equals("away"))rule="While going away from Campus";
        else rule="While Coming towards Campus";

        time=s.substring(i);

        if(time.equals("00_00"))time="Notify whenever bus passes";
        else if(time.equals("06_09"))time="Only between 6am - 9am";
        else if(time.equals("09_12"))time="Only between 9am - 12pm";
        else if(time.equals("12_15"))time="Only between 12pm - 3pm";
        else if(time.equals("15_18"))time="Only between 3pm - 6pm";
        else if(time.equals("18_22"))time="Only between 6pm - 10pm";


        viewHolder.timeName.setText(time);
        viewHolder.placeName.setText(place);
        viewHolder.ruleName.setText(rule);

    }

    @Override
    public int getItemCount() {
        return tokenList.size();
    }


    public class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView placeName,ruleName,timeName;
        public ImageButton deleteButton;
        public ViewHolder2(@NonNull View itemView) {
            super(itemView);
            placeName=itemView.findViewById(R.id.place_name);
            ruleName=itemView.findViewById(R.id.rule_name);
            timeName=itemView.findViewById(R.id.time_name);
            deleteButton=itemView.findViewById(R.id.delete_notification);
            deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int pos=getAdapterPosition();
            if(pos==RecyclerView.NO_POSITION){
                Toast.makeText(context,"Try again",Toast.LENGTH_LONG).show();
                return;
            }

            FirebaseMessaging.getInstance().unsubscribeFromTopic(tokenList.get(pos));
            SharedPreferences pref=context.getSharedPreferences("NOTIFICATIONS",Context.MODE_PRIVATE);
            Set<String> st=pref.getStringSet("tokenSet",new HashSet<>());
            Set<String> tmp=new HashSet<>(st);

            tmp.remove(tokenList.get(pos));
            pref.edit().clear();
            pref.edit().putStringSet("tokenSet",tmp);
            pref.edit().commit();

            tokenList.remove(pos);
            notifyDataSetChanged();

        }
    }

}
