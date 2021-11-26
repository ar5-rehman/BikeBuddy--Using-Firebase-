package com.techo.bikebuddy.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.techo.bikebuddy.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 0000;
    TextInputEditText userEmail, userPassword;
    TextInputLayout emailInputLayout, passInputLayout;
    Button login,register;
    TextView forgetPassword, refreshEmailVerificationLink;

    FirebaseAuth auth;
    ProgressBar progressBar;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;

    String email, pass, name, type, str_email = " ", str_pass = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        if (user != null ) {

            //checking the user email verification, either its verified or not
            if(user.isEmailVerified())
            {
                //verified
                Intent i = new Intent(LoginActivity.this, MainScreen.class);
                startActivity(i);
            }else{

                //not verified
            }

        } else {
            // User is signed out
        }

        userEmail = findViewById(R.id.user_email);
        userPassword = findViewById(R.id.user_pass);

        emailInputLayout = findViewById(R.id.userEmail);
        passInputLayout = findViewById(R.id.userpass);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);


        refreshEmailVerificationLink = findViewById(R.id.refreshToken);
        refreshEmailVerificationLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //calling the user defined function  for refreshing the email verification link, if the user forget to verify the email before an hour
                refreshEmailVerificationLink();
            }
        });

        forgetPassword = findViewById(R.id.userForgetPass);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmail.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    // userEmail.setError("Please enter a valid e-mail address!");
                    emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    emailInputLayout.setError("Please enter a valid e-mail address!");

                    return;
                }
                if (TextUtils.isEmpty(email)){
                    // userEmail.setError("Please fill the field!");
                    emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                    emailInputLayout.setError("Please fill the field!");
                    return;
                }

                //calling the function if the user may forget the password
                getUserEmailForgetPassword(email);
            }
        });

        login.setOnClickListener(this);
        register.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressbar);



    }

    @Override
    protected void onStart() {
        super.onStart();
        user = auth.getCurrentUser();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE)
        {

        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.login)
        {
            String Email = userEmail.getText().toString().trim();
            String Password = userPassword.getText().toString().trim();
            if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
            {
                // userEmail.setError("Please enter a valid e-mail address!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please enter a valid e-mail address!");

                return;
            }else{

                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);

            }
            if(TextUtils.isEmpty(Email)){
                // userEmail.setError("Please fill the field!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                emailInputLayout.setError("Please fill the field!");
                return;
            }else{
                emailInputLayout.setErrorEnabled(false);
                emailInputLayout.setBoxStrokeColor(Color.WHITE);
            }
            if (TextUtils.isEmpty(Password)){
                // userPassword.setError("Please fill the field!");
                passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                passInputLayout.setError("Please fill the field!");

                return;
            }else{
                passInputLayout.setErrorEnabled(false);
                passInputLayout.setBoxStrokeColor(Color.WHITE);
            }
            progressBar.setVisibility(View.VISIBLE);

            //calling to this user defined function to get the user email and password for checking that the user is entering the correct email and password or not
            getUserDetails(Email,Password);

        } else if(v.getId()==R.id.register)
        {
            Intent registerIntent = new Intent(LoginActivity.this, UserRegisterActivity.class);
            startActivity(registerIntent);
            userEmail.setText("");
            userPassword.setText("");
        }
    }

    //the function to send email for new password if the user forgets the password
    public void getUserEmailForgetPassword(String email)
    {
        firebaseFirestore.collection("user_data").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                String uemail = null, stremail = null;
                if (e != null) {

                } else {
                    for (DocumentChange documentChange : documentSnapshots.getDocumentChanges()) {
                        if (documentChange != null) {
                            uemail = documentChange.getDocument().getData().get("Email").toString();
                            if (email.equals(uemail)) {
                                stremail = uemail;
                            } else {

                            }
                        }
                    }

                    if (email.equals(stremail)) {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email sent!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Wrong Email!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    //the function to check the user email and password before login
    public void getUserDetails(String uemail, String upass){

        auth.signInWithEmailAndPassword(uemail, upass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            if (auth.getCurrentUser().isEmailVerified()) {
                                Intent intent = new Intent(getApplicationContext(), MainScreen.class);
                                startActivity(intent);
                                userEmail.setText("");
                                userPassword.setText("");
                            } else {
                                refreshEmailVerificationLink.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, "Please verify, check your Gmail account", Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            Toast.makeText(LoginActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                emailInputLayout.setError("Wrong Email!");
                emailInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
                passInputLayout.setError("Wrong Password!");
                passInputLayout.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            }
        });

    }

    //fucntion of refreshing the email verification link
    public void refreshEmailVerificationLink()
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
                    Toast.makeText(LoginActivity.this, "Something is wrong!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}