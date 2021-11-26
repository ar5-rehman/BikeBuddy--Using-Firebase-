package com.techo.bikebuddy.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.techo.bikebuddy.Activities.MainScreen;
import com.techo.bikebuddy.Fragments.ChatRoom;
import com.techo.bikebuddy.R;
import com.techo.bikebuddy.Models.RidePojo;

import java.util.ArrayList;
import java.util.List;

public class RidesAdapter extends RecyclerView.Adapter<RidesAdapter.MyViewHolder> implements Filterable {

    List<RidePojo> list;

    List<RidePojo> filterList;

    Context context;
    MainScreen activity;

    FirebaseFirestore firebaseFirestore;

    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference mDatabaseRef;

    String userName, checkJoin = " ", userImageUrl, joinedUsers;

    SharedPreferences ridePrefs;
    SharedPreferences.Editor editor;

    boolean check = false;

    public RidesAdapter(Context context, List<RidePojo> list, String userName, String userImageUrl){
        this.context = context;
        this.list = list;
        activity = (MainScreen) context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;

        database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        ridePrefs = this.context.getSharedPreferences("ride", Context.MODE_PRIVATE);
        editor = ridePrefs.edit();

        filterList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public RidesAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_layout, parent, false);
        RidesAdapter.MyViewHolder myViewHolder = new RidesAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RidesAdapter.MyViewHolder holder, int position) {
        RidePojo ridePojo = list.get(position);

        //getting all the data from data model class(RidePojo class) into the widgets
        holder.rideName.setText(ridePojo.getRideName());
        Glide.with(context).load(ridePojo.getLocationImage()).diskCacheStrategy( DiskCacheStrategy.DATA ).into(holder.locationImage);
        holder.startAddress.setText(ridePojo.getStartAddress());
        holder.destinationAddress.setText(ridePojo.getDestinationAddress());
        holder.distance.setText(ridePojo.getDistance()+" miles");
        holder.time.setText(ridePojo.getTime());
        holder.date.setText(ridePojo.getDate());

        joinedUsers(ridePojo.getRideID(), holder.leaveBtn);

        holder.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkUserJoining(ridePojo.getRideID(), ridePojo.getRideName(), holder.leaveBtn);

            }
        });

        //sending the message in the ride, also making sure that this user joins the ride before sending the message
        holder.msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] checkJoin2 = {" "};
                database = FirebaseDatabase.getInstance();
                mDatabaseRef = database.getReference().child(ridePojo.getRideID()).child("joinBy");
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dsp: dataSnapshot.getChildren()) {
                            checkJoin2[0] = dsp.child("user").getValue().toString();

                            if (checkJoin2[0].equals(userName)) {

                                check = true;

                            }

                        }

                        if(check == true){
                            ChatRoom bottomSheet = new ChatRoom(context, ridePojo.getRideID(), userName, userImageUrl, ridePojo.getRideName());
                            bottomSheet.show(activity.getSupportFragmentManager(),
                                    "ChatRoom");
                            check = false;
                        }else {
                            Toast.makeText(context, "First join the ride!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        //leaving the ride, and unsubscribing to the notification
        holder.leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query applesQuery = ref.child(ridePojo.getRideID()).child("joinBy").orderByChild("user").equalTo(userName);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();

                            String topic = ridePojo.getRideName().replace(" ", "");

                            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Left the ride successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });

                            holder.leaveBtn.setVisibility(View.INVISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });


    }

    //checking the joined users
    public void joinedUsers(String id, ImageView leaveBtn){
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference().child(id).child("joinBy");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    if(dsp.child("user").getValue().equals(userName)) {
                        leaveBtn.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //joining the ride, but before joining we are checking that this users is already joined the ride or not
    public void checkUserJoining(String id, String rideName, ImageView leaveBtn)
    {
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference().child(id).child("joinBy");
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    if(dsp.child("user").exists()) {
                        //making sure that this users is not joined, if joined then it will show a message that you are already joined
                        checkJoin = dsp.child("user").getValue().toString();
                    }
                }


                if(!checkJoin.equals(userName)){
                    mDatabaseRef = database.getReference().child(id).child("joinBy").push();
                    mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            mDatabaseRef.child("user").setValue(userName);
                            Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show();
                            leaveBtn.setVisibility(View.VISIBLE);

                            String topic = rideName.replace(" ", "");

                            //subscribing to the notification by the name of group
                            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                   // Toast.makeText(context,"Success",Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(context, "Already joined!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    //filtering or searching the rides by name
    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<RidePojo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (RidePojo item : filterList) {
                    if (item.getRideName().toLowerCase().contains(filterPattern)) {
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

        ImageView locationImage;
        TextView rideName, startAddress, destinationAddress, date, time, distance;
        ImageView joinBtn, msgBtn, leaveBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            locationImage = itemView.findViewById(R.id.locationImage);
            rideName = itemView.findViewById(R.id.rideName);
            startAddress = itemView.findViewById(R.id.location);
            destinationAddress = itemView.findViewById(R.id.destination);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            distance = itemView.findViewById(R.id.distance);

            joinBtn = itemView.findViewById(R.id.join);
            msgBtn = itemView.findViewById(R.id.chat);
            leaveBtn = itemView.findViewById(R.id.leave);
        }
    }
}