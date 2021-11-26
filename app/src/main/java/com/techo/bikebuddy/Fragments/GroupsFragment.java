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
import com.techo.bikebuddy.Adapters.GroupAdapter;
import com.techo.bikebuddy.Models.GroupPojo;
import com.techo.bikebuddy.R;

import java.util.ArrayList;
import java.util.List;

//Fragment class of all the group so the users can may join
public class GroupsFragment extends Fragment {


    public GroupAdapter adapter;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    List<GroupPojo> list;

    DatabaseReference ref;
    Context context;
    String userImageUrl;

    String groupName, desc, joinedUsers, groupPic, userName;
    long groupUsers;


    public GroupsFragment(Context context, String userName, String userImageUrl){
        this.context = context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        getActivity().setTitle("Groups");

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
                getGroupData();
            }
        });

        getGroupData();

        return view;
    }

    public void getGroupData()
    {
        list.clear();
        adapter = new GroupAdapter(context, list, userName, userImageUrl);
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
                            if(dataSnapshot.child(key).child("description").exists()) {

                                groupName = dataSnapshot.child(key).child("groupTitle").getValue().toString();
                                groupPic = dataSnapshot.child(key).child("groupPic").getValue().toString();
                                desc = dataSnapshot.child(key).child("description").getValue().toString();
                                groupUsers = dataSnapshot.child(key).child("joinedUsers").getChildrenCount();

                                list.add(new GroupPojo(groupName, groupUsers, groupPic, desc));
                                adapter = new GroupAdapter(context, list, userName, userImageUrl);
                                recyclerView.setAdapter(adapter);
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