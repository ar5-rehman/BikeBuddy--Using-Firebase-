package com.techo.bikebuddy.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techo.bikebuddy.Adapters.GroupAdapter;
import com.techo.bikebuddy.Models.GroupPojo;
import com.techo.bikebuddy.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

//This fragment class is to create new group
public class CreateGroupFragment extends Fragment {

    private static final int MY_PERMISSION_STORAGE = 112;

    Context context;

    EditText groupName, description;
    Button create;
    ImageView picture, pictureOfGroup;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "", checkGroupName;

    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    StorageReference fireRef;
    StorageReference mStorageRef;

    String name;

    String userName, userImageUrl;

    public CreateGroupFragment(Context context, String userName, String userImageUrl) {
        this.context = context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        getActivity().setTitle("Create Group");

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        groupName = view.findViewById(R.id.groupName);
        description = view.findViewById(R.id.description);

        create = view.findViewById(R.id.create);
        picture = view.findViewById(R.id.picture);
        pictureOfGroup = view.findViewById(R.id.pictureOfPlace);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_STORAGE);

                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1) //You can skip this for free form aspect ratio)
                            .start(getActivity());
                }
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupTitle = groupName.getText().toString();
                String desc = description.getText().toString();

                if(groupTitle.isEmpty()){
                    groupName.setError("Cannot be empty!");
                    return;
                }
                if(desc.isEmpty()){
                    description.setError("Cannot be empty!");
                    return;
                }

                if (pictureOfGroup.getDrawable() == null) {
                    Toast.makeText(getContext(), "Please, select a location photo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadGroupData(groupTitle, desc);


            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try{

                    Uri resultUri = result.getUri();
                    imageUri = resultUri;
                    imageStream = getActivity().getContentResolver().openInputStream(resultUri);
                    selectedImage = BitmapFactory.decodeStream(imageStream);
                    pictureOfGroup.setImageBitmap(selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    //Creating new group
    public void uploadGroupData(String groupTitle, String desc)
    {
        try {

            String strFileName = user.getUid() + ".jpg";

            Uri file = imageUri;

            fireRef = mStorageRef.child("images/" + "group " + user.getUid().toString() + "/" + strFileName);

            UploadTask uploadTask = fireRef.putFile(file);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fireRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        imageURL = downloadUri.toString();
                        selectedImage = null;

                         firebaseDatabase.getReference(groupTitle).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    groupName.setError("Group title already exists!");
                                }else{
                                    ref = firebaseDatabase.getReference(groupTitle);
                                    GroupPojo data = new GroupPojo();
                                    data.setGroupPic(imageURL);
                                    data.setGroupTitle(groupTitle);
                                    data.setDescription(desc);
                                    data.setJoinedUsers("");
                                    ref.setValue(data);

                                    Toast.makeText(getContext(), "Congrats, Group is created successfully!", Toast.LENGTH_LONG).show();

                                    Fragment frag = new GroupsFragment(context, userName, userImageUrl);
                                    if (frag != null) {
                                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                        transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                                        transaction.commit(); // commit the changes
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });




                    } else {

                        Toast.makeText(getContext(), "Something is wrong!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {

        }
    }

}