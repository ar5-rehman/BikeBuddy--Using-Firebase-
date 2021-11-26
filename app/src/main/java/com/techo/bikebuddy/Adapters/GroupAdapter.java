package com.techo.bikebuddy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import com.techo.bikebuddy.Models.GroupPojo;
import com.techo.bikebuddy.R;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> implements Filterable {

    List<GroupPojo> list;

    List<GroupPojo> filterList;

    Context context;
    MainScreen activity;

    FirebaseFirestore firebaseFirestore;

    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference mDatabaseRef;

    String userName, checkJoin = " ", userImageUrl;

    boolean check = false;

    long groupUsers;

    public GroupAdapter(Context context, List<GroupPojo> list, String userName, String userImageUrl){
        this.context = context;
        this.list = list;
        activity = (MainScreen) context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;


        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        filterList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public GroupAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_layout, parent, false);
        GroupAdapter.MyViewHolder myViewHolder = new GroupAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.MyViewHolder holder, int position) {
        GroupPojo groupPojo = list.get(position);

        //getting all the data from data model class( GroupPojo class) into the widgets
        holder.groupName.setText(groupPojo.getGroupTitle());
        Glide.with(context).load(groupPojo.getGroupPic()).diskCacheStrategy( DiskCacheStrategy.DATA ).into(holder.locationImage);
        holder.description.setText(groupPojo.getDescription());
        holder.joinedUsers.setText(""+groupPojo.getGroupUsers());

        joinedUsers(groupPojo.getGroupTitle(), holder.leaveBtn);

        //users to join by clicking this button
        holder.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkUserJoining(groupPojo.getGroupTitle(), holder.leaveBtn, holder.joinedUsers);

            }
        });


        //sending the message in the group, also making sure that this user joins the group before sending the message
        holder.cmntBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String[] checkJoin2 = {" "};
                database = FirebaseDatabase.getInstance();
                mDatabaseRef = database.getReference().child(groupPojo.getGroupTitle()).child("joinedUsers");
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dsp: dataSnapshot.getChildren()){
                                checkJoin2[0] = dsp.child("user").getValue().toString();
                        }

                        if(checkJoin2[0].equals(userName)) {

                            check = true;

                        }

                        if(check==true){
                            ChatRoom bottomSheet = new ChatRoom(context, groupPojo.getGroupTitle(), userName, userImageUrl,  groupPojo.getGroupTitle());
                            bottomSheet.show(activity.getSupportFragmentManager(),
                                    "ChatRoom");
                            check = false;

                        }else{
                            Toast.makeText(context, "First join the group!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        //leaving the group, and unsubscribing to the notification
        holder.leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                Query applesQuery = ref.child(groupPojo.getGroupTitle()).child("joinedUsers").orderByChild("user").equalTo(userName);

                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();

                            countUsers(groupPojo.getGroupTitle(), holder.joinedUsers);

                            String topic = groupPojo.getGroupTitle().replace(" ", "");

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
        mDatabaseRef = database.getReference().child(id).child("joinedUsers");
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

    //joining the group, but before joining we are checking that this users is already joined the group or not
    public void checkUserJoining(String id, ImageView leaveBtn, TextView users)
    {
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference().child(id).child("joinedUsers");
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
                    mDatabaseRef = database.getReference().child(id).child("joinedUsers").push();
                    mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            mDatabaseRef.child("user").setValue(userName);
                            Toast.makeText(context, "Joined successfully!", Toast.LENGTH_SHORT).show();
                            leaveBtn.setVisibility(View.VISIBLE);

                            countUsers(id, users);

                            String topic = id.replace(" ", "");

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

    //count the total joined users in the group
    public void countUsers(String key, TextView users){

        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference().child(key);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        groupUsers = dataSnapshot.child("joinedUsers").getChildrenCount();
                        users.setText(""+groupUsers);            }

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

    //filtering or searching the groups by name
    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<GroupPojo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (GroupPojo item : filterList) {
                    if (item.getGroupTitle().toLowerCase().contains(filterPattern)) {
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
        TextView groupName, description, joinedUsers;
        ImageView joinBtn, cmntBtn, leaveBtn;
        CardView group;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            locationImage = itemView.findViewById(R.id.locationImage);
            groupName = itemView.findViewById(R.id.groupName);
            description = itemView.findViewById(R.id.desc);
            joinedUsers = itemView.findViewById(R.id.joinedUsers);

            joinBtn = itemView.findViewById(R.id.join);
            cmntBtn = itemView.findViewById(R.id.chat);
            leaveBtn = itemView.findViewById(R.id.leave);

            group = itemView.findViewById(R.id.cardView);

        }
    }
}