package com.techo.bikebuddy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techo.bikebuddy.Adapters.MsgsAdapter;
import com.techo.bikebuddy.Models.MsgsPojo;
import com.techo.bikebuddy.MySingleton;
import com.techo.bikebuddy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom extends BottomSheetDialogFragment {

    //the firebase cloud messeging
    //the api for sending the nofication to the users
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    //the server key from the firebase
    final private String serverKey = "key=" + ".........BitiQ6a3FAyjW_SQw7XCLCAt-37rM3usWIqUYQRvq9lFUezNjrrvnp-CQf8NfmNK";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String TOPIC;
    private String format = "";

    EditText msgBox;
    ImageView send;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    ArrayList<MsgsPojo> list;

    FirebaseDatabase database;
    DatabaseReference mDatabaseRef;
    Context context;
    String userId;
    String userName, userImage;
    String rideORgroupName;

    public static boolean sender = false;

    public ChatRoom(Context context, String userId, String userName, String userImage, String rideORgroupName){
        this.context = context;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.rideORgroupName = rideORgroupName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.msg_bottomsheet_layout,
                container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.msg_recyclerview);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        msgBox = v.findViewById(R.id.msg_box);
        send = v.findViewById(R.id.send);

        database = FirebaseDatabase.getInstance();

        //sending the message to the ride or group
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = msgBox.getText().toString();

                mDatabaseRef = database.getReference().child(userId).child("ChatRoom").push();

                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dsp) {
                        mDatabaseRef.child("userMessage").setValue(msg);
                        mDatabaseRef.child("userName").setValue(userName);
                        mDatabaseRef.child("userImage").setValue(userImage);

                        sender = true;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                msgBox.setText("");
                sendPushNotification(userName, msg, userImage);
            }
        });

        getMsgs();

        return v;
    }

    //receving the messages
    public void getMsgs(){

        mDatabaseRef = database.getReference().child(userId).child("ChatRoom");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for (DataSnapshot dsp: dataSnapshot.getChildren()){
                    if(dsp.child("userMessage").exists() && dsp.child("userName").exists() && dsp.child("userImage").exists()) {
                        String msg = dsp.child("userMessage").getValue().toString();
                        String name = dsp.child("userName").getValue().toString();
                        String image = dsp.child("userImage").getValue().toString();


                        list.add(new MsgsPojo(msg, name, image));
                        adapter = new MsgsAdapter(list, context);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //sending the notification with the (message, title, user image) to the specific user by using the topic subscription methodolgy
    public void sendPushNotification(String title, String msg, String imageUrl)
    {
        TOPIC = "/topics/"+rideORgroupName; //topic must match with what the receiver subscribed to

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", title);
            notifcationBody.put("msg", msg);
            notifcationBody.put("imageUrl", imageUrl);

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {

        }
        sendNotification(notification);
    }


    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
