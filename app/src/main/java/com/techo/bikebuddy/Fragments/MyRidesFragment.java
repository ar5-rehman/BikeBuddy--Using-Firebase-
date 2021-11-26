package com.techo.bikebuddy.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techo.bikebuddy.Fragments.JoinedUsersFragment;
import com.techo.bikebuddy.R;

//The fragment class to get user ride as an admin
public class MyRidesFragment extends Fragment {

    ImageView locationImage;
    TextView rideName, location, date, time, distance, destination;
    Button joinUsersBtn;

    FirebaseUser user;
    DatabaseReference ref;

    Fragment frag;

    String rideID, ride_name, loc_pic, startAddress, destinationAddress, ride_date, ride_time, ride_distance, ride_loc;

    Context context;

    public MyRidesFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_rides, container, false);

        getActivity().setTitle("My Rides");

        locationImage = view.findViewById(R.id.locationImage);
        rideName = view.findViewById(R.id.rideName);
        location = view.findViewById(R.id.location);
        date = view.findViewById(R.id.date);
        time = view.findViewById(R.id.time);
        distance = view.findViewById(R.id.distance);
        destination = view.findViewById(R.id.destination);

        joinUsersBtn = view.findViewById(R.id.joinBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();
        ref= FirebaseDatabase.getInstance().getReference(user.getUid());

        getRidesData();

        joinUsersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!rideID.isEmpty()) {
                    frag = new JoinedUsersFragment(context, rideID);
                    if (frag != null) {
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                        transaction.commit(); // commit the changes
                    }
                }

            }
        });

        return view;
    }



    public void getRidesData()
    {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.child("startAddress").exists()) {

                                rideID = dataSnapshot.child("rideID").getValue().toString();
                                ride_name = dataSnapshot.child("rideName").getValue().toString();
                                loc_pic = dataSnapshot.child("locationImage").getValue().toString();
                                startAddress = dataSnapshot.child("startAddress").getValue().toString();
                                destinationAddress = dataSnapshot.child("destinationAddress").getValue().toString();
                                ride_time = dataSnapshot.child("time").getValue().toString();
                                ride_date = dataSnapshot.child("date").getValue().toString();
                                ride_distance = dataSnapshot.child("distance").getValue().toString();


                                rideName.setText(ride_name);
                                location.setText(startAddress);
                                destination.setText(destinationAddress);
                                date.setText(ride_date);
                                time.setText(ride_time);
                                distance.setText(ride_distance+" miles");
                                Glide.with(context).load(loc_pic).diskCacheStrategy( DiskCacheStrategy.DATA ).into(locationImage);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
    }

}