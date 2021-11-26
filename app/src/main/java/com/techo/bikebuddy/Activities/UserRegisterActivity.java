package com.techo.bikebuddy.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.techo.bikebuddy.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSION_STORAGE = 1112;

    TextInputEditText name,email,pass,conpass;
    TextInputLayout userName, userEmail, userPass, userCpass;
    Button register;
    FirebaseUser user;
    FirebaseAuth auth;
    ProgressBar progressBar;
    ImageView userProfilePic, picture;
    String str_name,str_email, str_password, str_confirm_password;

    StorageReference fireRef;
    StorageReference mStorageRef;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "";

    FirebaseFirestore firebaseFirestore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Registration");

        mStorageRef = FirebaseStorage.getInstance().getReference();

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPass = findViewById(R.id.userPass);
        userCpass = findViewById(R.id.conpass);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        pass  =findViewById(R.id.pass);
        conpass = findViewById(R.id.cpass);
        register = findViewById(R.id.signup);
        progressBar = findViewById(R.id.progressbar);

        picture = findViewById(R.id.picture);
        userProfilePic = findViewById(R.id.profilePicture);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //asking for the camera and galery permissions before selecting the picture from the camera or gallery
                if (ContextCompat.checkSelfPermission(UserRegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(UserRegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UserRegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);
                    ActivityCompat.requestPermissions(UserRegisterActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_STORAGE);

                } else {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1) //You can skip this for free form aspect ratio)
                            .start(UserRegisterActivity.this);
                }
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        register.setOnClickListener(this);
    }

    //the picture will finally came to user screen after selecting from the galllery or from camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try{

                    Uri resultUri = result.getUri();
                    imageUri = resultUri;
                    imageStream = getContentResolver().openInputStream(resultUri);
                    selectedImage = BitmapFactory.decodeStream(imageStream);
                    userProfilePic.setImageBitmap(selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onClick(View v) {

        str_name = name.getText().toString().trim();
        str_email = email.getText().toString().trim();
        str_password = pass.getText().toString().trim();
        str_confirm_password = conpass.getText().toString().trim();

        //all conditions before registring the new user, the fields should not be null or empty
        //the image should be selected
        //the email should be properly write by user
        //the password should be in 8 characters
        if(userProfilePic.getDrawable() == null) {
            Toast.makeText(UserRegisterActivity.this, "Please, select a profile photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(str_name)) {
            userName.setError("Please fill the field!");
            userName.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        } else{
            userName.setErrorEnabled(false);
            userName.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_email)) {
            userEmail.setError("Please fill the field!");
            userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userEmail.setErrorEnabled(false);
            userEmail.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_password)) {
            userPass.setError("Please fill the field!");
            userPass.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }
        else{
            userPass.setErrorEnabled(false);
            userPass.setBoxStrokeColor(Color.WHITE);
        }
        if (TextUtils.isEmpty(str_confirm_password)) {
            userCpass.setError("Please fill the field!");
            userCpass.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
            userEmail.setError("Please enter a valid e-mail address!");
            userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            return;
        }else{
            userEmail.setErrorEnabled(false);
            userEmail.setBoxStrokeColor(Color.WHITE);
        }
        if (str_password.length() < 8 && str_confirm_password.length() < 8) {
            userPass.setError("Password must contains 8 alphabet/digit/symbol!");
            userCpass.setError("Password must contains 8 alphabet/digit/symbol!");
            return;
        }else{
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }
        if (!str_password.equals(str_confirm_password)) {
            userPass.setError("Password mismatched!");
            userCpass.setError("Password mismatched!");
            return;
        }
        else{
            userPass.setErrorEnabled(false);
            userPass.setBoxStrokeColor(Color.WHITE);
            userCpass.setErrorEnabled(false);
            userCpass.setBoxStrokeColor(Color.WHITE);
        }

        //registering the user
        registering(str_name);
    }

    //sending the verification link to the user gmail address
    public void checkEmailVerification()
    {

        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(), "Please check your Gmail account for verification and login once!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(UserRegisterActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    //registering the user
    public void registering(String userName) {
        progressBar.setVisibility(View.VISIBLE);
        if (str_password.equals(str_confirm_password)) {
            auth.createUserWithEmailAndPassword(str_email, str_confirm_password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {

                                userID = auth.getCurrentUser().getUid();

                                getProfilePicUrl(userName, userID);


                            } else {
                                task.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserRegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                });
                                userEmail.setError("This Gmail account is already taken!");
                                userEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                                // Toast.makeText(UserRegisterActivity.this, "This Gmail account is already taken!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }
    }

    //uploading the user picture to the firebase storage
    public void getProfilePicUrl(String userName, String userID){
        try {

            String strFileName = userName + ".jpg";

            Uri file = imageUri;

            fireRef = mStorageRef.child("images/" + "userImages " + userID + "/" + strFileName);

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

                        DocumentReference documentReference = firebaseFirestore.collection("user_data").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("ProfilePic", imageURL);
                        user.put("Name", str_name);
                        user.put("Email", str_email);
                        user.put("Password", str_password);
                        user.put("UserID", userID);
                        user.put("Image", " ");
                        documentReference.set(user);

                        Intent intent = new Intent(UserRegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        checkEmailVerification();

                      //  Toast.makeText(UserRegisterActivity.this, "Congrats, Ride is created successfully!", Toast.LENGTH_LONG).show();

                    } else {

                        Toast.makeText(UserRegisterActivity.this, "Something is wrong!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {
             Toast.makeText(UserRegisterActivity.this, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }
}

