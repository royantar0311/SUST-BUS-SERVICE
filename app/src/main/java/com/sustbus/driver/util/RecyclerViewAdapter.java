package com.sustbus.driver.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    private Context context;
    private List<RouteInformation> contactList;

    public RecyclerViewAdapter(Context context, List<RouteInformation> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view=null; //LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
//        Contact contact = contactList.get(position); // each contact object inside of our list
//
//        viewHolder.contactName.setText(contact.getName());
//        viewHolder.phoneNumber.setText(contact.getPhoneNumber());
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);



        }

        @Override
        public void onClick(View v) {


        }
    }

}
