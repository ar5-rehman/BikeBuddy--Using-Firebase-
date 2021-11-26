package com.techo.bikebuddy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.techo.bikebuddy.Models.MsgsPojo;
import com.techo.bikebuddy.R;

import java.util.List;

//This class is for messages (sending and receiving)
public class MsgsAdapter extends RecyclerView.Adapter<MsgsAdapter.MyViewHolder>{

    List<MsgsPojo> list;
    Context context;


    public MsgsAdapter(List<MsgsPojo> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        MsgsPojo msgsPojo = list.get(position);
        String msg = msgsPojo.getMsg();
        String name = msgsPojo.getUserName();
        String image = msgsPojo.getUserImage();
        holder.msg.setText(msg);
        holder.name.setText(name);
        if(image.equals("")){
            holder.image.setImageResource(R.drawable.user);
        }else {
            Glide.with(context).load(image).diskCacheStrategy(DiskCacheStrategy.DATA).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView msg, name;
        ImageView image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            msg = itemView.findViewById(R.id.msg);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
        }
    }
}
