package com.sustbus.driver.util;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sustbus.driver.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    private Context context;
    public static final int  FINISHED=-1;
    public static final int NEXT=1;
    public static final int ON_ROAD=2;
    private List<RouteInformation> routeList;
    private ClickEvent callBack;
    private int id;

    public RecyclerViewAdapter(Context context, List<RouteInformation> routeList,ClickEvent callBack,int from) {
        this.context = context;
        this.routeList=routeList;
        this.callBack=callBack;
        this.id=from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout_recycler_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
      viewHolder.detailsTv.setText(routeList.get(position).showPath());
      viewHolder.titleTv.setText(routeList.get(position).getTitle());
      viewHolder.timeTv.setText(routeList.get(position).getTime());

      if(id==ON_ROAD){
          if(routeList.get(position).getMarkerId()==null){
              viewHolder.rowCardview.setCardBackgroundColor(ContextCompat.getColor(context,R.color.smothRed));
          }
          else{
              viewHolder.rowCardview.setCardBackgroundColor(ContextCompat.getColor(context,R.color.greenSignal));
          }

      }

    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }


    public interface ClickEvent{
        void click(int position, int from);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView timeTv,titleTv,detailsTv;
        public CardView rowCardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeTv=itemView.findViewById(R.id.time_text_view);
            titleTv=itemView.findViewById(R.id.title_text_view);
            detailsTv=itemView.findViewById(R.id.details_text_view);
            rowCardview=itemView.findViewById(R.id.row_card_view);
            rowCardview.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(callBack!=null){
                callBack.click(getLayoutPosition(),id);
            }
        }
    }

}
