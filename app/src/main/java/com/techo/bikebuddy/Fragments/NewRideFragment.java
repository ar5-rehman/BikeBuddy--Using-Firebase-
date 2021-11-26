package com.techo.bikebuddy.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
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
import com.techo.bikebuddy.Activities.MainScreen;
import com.techo.bikebuddy.Adapters.AutoCompleteAdapter;
import com.techo.bikebuddy.Adapters.RidesAdapter;
import com.techo.bikebuddy.Models.NewRideData;
import com.techo.bikebuddy.Models.RidePojo;
import com.techo.bikebuddy.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

//the fragment class to create new ride
public class NewRideFragment extends Fragment {

    private static final int MY_PERMISSION_STORAGE = 1112;

    AutoCompleteTextView startAddress, destinationAddress;
    AutoCompleteAdapter adapter;
    PlacesClient placesClient;

    EditText dateOfRide, time, distance, rideName;
    Button create;
    ImageView picture, pictureOfPlace;

    Bitmap selectedImage;
    InputStream imageStream;
    Uri imageUri;
    String imageURL = "", checkRideName;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    Context context;

    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    StorageReference fireRef;
    StorageReference mStorageRef;

    boolean present = false;
    List<String> namesList;

    String userName, userImageUrl;

    public NewRideFragment(Context context, String userName, String userImageUrl) {
        // Required empty public constructor
        this.context = context;
        this.userName = userName;
        this.userImageUrl = userImageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_ride, container, false);

        getActivity().setTitle("Create Ride");

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        namesList = new ArrayList<>();
        checkRidesName();

        String apiKey = getString(R.string.api_key);
        if(apiKey.isEmpty()){

            return null;
        }

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey);
        }

        placesClient = Places.createClient(context);
        startAddress = view.findViewById(R.id.auto);
        destinationAddress = view.findViewById(R.id.destination);

        dateOfRide = view.findViewById(R.id.date);
        dateOfRide.setFocusable(false);
        dateOfRide.setKeyListener(null);
        time = view.findViewById(R.id.timing);
        time.setFocusable(false);
        time.setKeyListener(null);
        distance = view.findViewById(R.id.distance);
        rideName = view.findViewById(R.id.rideName);

        create = view.findViewById(R.id.create);
        picture = view.findViewById(R.id.picture);
        pictureOfPlace = view.findViewById(R.id.pictureOfPlace);

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

        dateOfRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, date,
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDOB();
            }
        };

        startAddressFun();
        destinationAddressFun();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String rideNam = rideName.getText().toString();
                String sourceAddress = startAddress.getText().toString();
                String destination = destinationAddress.getText().toString();
                String timeOfRide = time.getText().toString();
                String distanceOfLocation = distance.getText().toString();
                String rideDate = dateOfRide.getText().toString();

                if(rideNam.isEmpty()){
                    rideName.setError("Cannot be empty!");
                    return;
                }

                if(sourceAddress.isEmpty()){
                    startAddress.setError("Cannot be empty!");
                    return;
                }

                if(destination.isEmpty()){
                    destinationAddress.setError("Cannot be empty!");
                    return;
                }

                if(timeOfRide.isEmpty()){
                    time.setError("Cannot be empty!");
                    return;
                }

                if(distanceOfLocation.isEmpty()){
                    distance.setError("Cannot be empty!");
                    return;
                }
                if(rideDate.isEmpty()){
                    dateOfRide.setError("Cannot be empty!");
                    return;
                }

                if (pictureOfPlace.getDrawable() == null) {
                    Toast.makeText(getContext(), "Please, select a location photo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(rideNam.equals(checkRideName)){
                    rideName.setError("Ride name already taken!");
                    return;
                }
                boolean chk = false;
                String n = null;

                for(int i=0; i<namesList.size();i++){
                    if(namesList.get(i).equals(rideNam)){
                        rideName.setError("Ride name already taken!");
                        n = namesList.get(i);
                        break;
                    }else{
                        chk=true;
                    }
                }

                if(chk==true && !rideNam.equals(n)) {
                    uploadRideData(rideNam, sourceAddress, destination, distanceOfLocation, timeOfRide, rideDate);
                }
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
                    pictureOfPlace.setImageBitmap(selectedImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void uploadRideData(String rideName, String startAddress, String destinationAddress, String distance, String time, String date)
    {
        try {

            String strFileName = user.getUid() + ".jpg";

            Uri file = imageUri;

            fireRef = mStorageRef.child("images/" + "rides " + user.getUid().toString() + "/" + strFileName);

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

                        ref = firebaseDatabase.getReference(user.getUid());
                        NewRideData data = new NewRideData();
                        data.setLocationImage(imageURL);
                        data.setRideName(rideName);
                        data.setStartAddress(startAddress);
                        data.setDestinationAddress(destinationAddress);
                        data.setDistance(distance);
                        data.setTime(time);
                        data.setDate(date);
                        data.setJoinBy("");
                        data.setMessages("");
                        data.setRideID(user.getUid());
                        ref.setValue(data);

                        Toast.makeText(getContext(), "Congrats, Ride is created successfully!", Toast.LENGTH_LONG).show();

                        Fragment frag = new RidesFragment(context, userName, userImageUrl);

                        if (frag != null) {
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                            transaction.commit(); // commit the changes
                        }

                    } else {

                        Toast.makeText(getContext(), "Something is wrong!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        } catch (Exception ex) {
            // Toast.makeText(getContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void showTimePicker(){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(context , new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

               /* String AM_PM ;
                if(selectedHour < 12) {
                    AM_PM = "AM";
                } else {
                    AM_PM = "PM";
                }
*/
                time.setText(selectedHour+" : "+selectedMinute+ " ");
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void updateDOB () {
        String myFormat = "MM-dd-yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateOfRide.setText(sdf.format(myCalendar.getTime()));
    }

    private void startAddressFun() {

        startAddress.setThreshold(1);
        startAddress.setOnItemClickListener(autocompleteClickListener);
        adapter = new AutoCompleteAdapter(context, placesClient);
        startAddress.setAdapter(adapter);

    }

    private void destinationAddressFun() {

        destinationAddress.setThreshold(1);
        destinationAddress.setOnItemClickListener(autocompleteClickListener);
        adapter = new AutoCompleteAdapter(context, placesClient);
        destinationAddress.setAdapter(adapter);

    }

    private AdapterView.OnItemClickListener autocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            try {
                final AutocompletePrediction item = adapter.getItem(i);
                String placeID = null;
                if (item != null) {
                    placeID = item.getPlaceId();
                }

                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS
                        , Place.Field.LAT_LNG);

                FetchPlaceRequest request = null;
                if (placeID != null) {
                    request = FetchPlaceRequest.builder(placeID, placeFields)
                            .build();
                }

                if (request != null) {
                    placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSuccess(FetchPlaceResponse task) {
                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void checkRidesName()
    {
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

                                checkRideName = dataSnapshot.child(key).child("rideName").getValue().toString();

                                namesList.add(checkRideName);

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

    }
}