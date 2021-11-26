package com.techo.bikebuddy.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techo.bikebuddy.Adapters.RidesAdapter;
import com.techo.bikebuddy.R;
import com.techo.bikebuddy.Models.RidePojo;

import java.util.ArrayList;
import java.util.List;

//the fragment to show all the ride to the users
public class RidesFragment extends Fragment {

    public RidesAdapter adapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    List<RidePojo> list;

    DatabaseReference ref;
    Context context;
    String userImageUrl;

    String rideID, userName, rideName, locationPic, startAddress, destinationAddress , distance, time, date;

    public RidesFragment(Context context, String userName, String userImageUrl){
        this.context = context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rides, container, false);

        getActivity().setTitle("Rides");

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList();

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRidesData();
            }
        });

        getRidesData();

        return view;
    }

    public void getRidesData()
    {
        list.clear();
        adapter = new RidesAdapter(context, list, userName, userImageUrl);
        recyclerView.setAdapter(adapter);

        ref= FirebaseDatabase.getInstance().getReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    String key = dsp.getKey();

                    ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dsp) {
                            if(dataSnapshot.child(key).child("startAddress").exists()) {

                                rideID = dataSnapshot.child(key).child("rideID").getValue().toString();
                                rideName = dataSnapshot.child(key).child("rideName").getValue().toString();
                                locationPic = dataSnapshot.child(key).child("locationImage").getValue().toString();
                                startAddress = dataSnapshot.child(key).child("startAddress").getValue().toString();
                                destinationAddress = dataSnapshot.child(key).child("destinationAddress").getValue().toString();
                                time = dataSnapshot.child(key).child("time").getValue().toString();
                                date = dataSnapshot.child(key).child("date").getValue().toString();
                                distance = dataSnapshot.child(key).child("distance").getValue().toString();

                                list.add(new RidePojo(rideID, rideName, locationPic, startAddress, destinationAddress, time, distance, date));
                                adapter = new RidesAdapter(context, list, userName, userImageUrl);
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        if(list.size()==0) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.user_main_screen, menu);
        MenuItem searchItem = menu.findItem(R.id.action_settings);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}