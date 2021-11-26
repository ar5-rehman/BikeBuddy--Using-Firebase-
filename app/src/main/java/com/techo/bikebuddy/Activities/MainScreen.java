package com.techo.bikebuddy.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.techo.bikebuddy.Fragments.CreateGroupFragment;
import com.techo.bikebuddy.Fragments.GroupsFragment;
import com.techo.bikebuddy.Fragments.MyRidesFragment;
import com.techo.bikebuddy.Fragments.NewRideFragment;
import com.techo.bikebuddy.Fragments.UserProfile;
import com.techo.bikebuddy.R;
import com.techo.bikebuddy.Fragments.RidesFragment;

public class MainScreen extends AppCompatActivity {

    DrawerLayout drawer;
    TextView headerUserName, headerUserEmail;
    ImageView userProfileImage;
    Menu menu;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser user;

    private String userName, userImageUrl, userEmail;

    Fragment frag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //initializing all the widgets
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bike Buddy");

        drawer = findViewById(R.id.artist_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        NavigationView navigationView = findViewById(R.id.user_nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        headerUserName = headerView.findViewById(R.id.headerUserName);
        headerUserEmail = headerView.findViewById(R.id.headerUserEmail);
        userProfileImage = headerView.findViewById(R.id.userProfileImage);

        //updating the user profile
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag = new UserProfile(MainScreen.this);
                if (frag != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                    transaction.commit(); // commit the changes
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        menu = navigationView.getMenu();

        //all the menu items and actions from the side navigation tool
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.nav_logout)
                {

                    FirebaseAuth.getInstance().signOut();

                    headerUserName.setText(" ");
                    headerUserEmail.setText(" ");
                    menu.findItem(R.id.nav_logout).setTitle("Login");

                    Intent logout = new Intent(MainScreen.this, LoginActivity.class);
                    startActivity(logout);

                }else if(item.getItemId()==R.id.nav_home){

                    if(userName!=null && userImageUrl!=null) {
                        frag = new RidesFragment(MainScreen.this, userName, userImageUrl);
                    }

                }else if(item.getItemId()==R.id.nav_my_rides){

                    frag = new MyRidesFragment(MainScreen.this);

                }else if(item.getItemId()==R.id.nav_new_ride){

                    if(userName!=null && userImageUrl!=null) {
                        frag = new NewRideFragment(MainScreen.this, userName, userImageUrl);
                    }

                }else if(item.getItemId()==R.id.nav_new_group){

                    if(userName!=null && userImageUrl!=null) {
                        frag = new CreateGroupFragment(MainScreen.this, userName, userImageUrl);
                    }

                }else if(item.getItemId()==R.id.nav_group){

                    if(userName!=null && userImageUrl!=null) {
                        frag = new GroupsFragment(MainScreen.this, userName, userImageUrl);
                    }

                }

                if (frag != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                    transaction.commit(); // commit the changes
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //calling this function to get the users data from firebase like, user name, user image and email
        getUserData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    //here is the function mentioned above to get the users data like user name, image, and email
    public void getUserData()
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e !=null)
                {

                }
                else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if(documentChange.getDocument().exists()) {
                            String userEmail = documentChange.getDocument().getData().get("Email").toString();
                            String userProfileName = documentChange.getDocument().getData().get("Name").toString();
                            String profileImageURL = documentChange.getDocument().getData().get("ProfilePic").toString();

                            if(user.getEmail().equals(userEmail)) {
                                userName = userProfileName;
                                headerUserName.setText(userProfileName);
                                headerUserEmail.setText(userEmail);

                                if(profileImageURL.equals(" "))
                                {
                                    userImageUrl = "";
                                }else {
                                    userImageUrl = profileImageURL;
                                    Glide.with(getApplicationContext()).load(profileImageURL).diskCacheStrategy( DiskCacheStrategy.DATA ).into(userProfileImage);
                                }

                                frag = new RidesFragment(MainScreen.this, userName, userImageUrl);
                                if (frag != null) {
                                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                                    transaction.commit(); // commit the changes
                                }
                            }
                        }
                    }
                }
            }
        });
    }

}