package com.techo.bikebuddy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.techo.bikebuddy.Models.JoinedUsersPojo;
import com.techo.bikebuddy.R;

import java.util.ArrayList;
import java.util.List;

//this class is for getting all the joined users who joined the ride
public class JoinUsersAdapter extends RecyclerView.Adapter<JoinUsersAdapter.MyViewHolder> implements Filterable {

    List<JoinedUsersPojo> list;
    List<JoinedUsersPojo> filterList;
    Context context;


    public JoinUsersAdapter(Context context, List<JoinedUsersPojo> list){
        this.context = context;
        this.list = list;

        filterList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public JoinUsersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.join_users_layout, parent, false);
        JoinUsersAdapter.MyViewHolder myViewHolder = new JoinUsersAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull JoinUsersAdapter.MyViewHolder holder, int position) {
        JoinedUsersPojo ridePojo = list.get(position);

        holder.userName.setText(ridePojo.getUserName());
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<JoinedUsersPojo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (JoinedUsersPojo item : filterList) {
                    if (item.getUserName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView userName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
        }
    }
}
