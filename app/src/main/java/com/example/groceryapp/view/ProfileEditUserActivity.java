package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.EditUserProfileClicks;
import com.example.groceryapp.databinding.ActivityProfileEditUserBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ProfileEditUserActivity extends AppCompatActivity implements LocationListener {
    ActivityProfileEditUserBinding binding;
    private Uri image_uri;
    //permission Constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //IMAGE PICK CONSTANTS
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //PERMISSION ARRAYS..
    private String locationPermission[];
    private String cameraPermission[];
    private String storagePermission[];
    private double latitude = 0.0;
    private double longitude = 0.0;
    //progress dialog
    private ProgressDialog progressDialog;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile_edit_user);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile_edit_user);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        binding.setClickHandle(new EditUserProfileClicks() {
            @Override
            public void editUserProfileBackbtn() {
                onBackPressed();
            }

            @Override
            public void editUserProfileGpsBtnClick() {
                //detect location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed,,,request for location permission
                    requestLocationPermission();
                }

            }

            @Override
            public void editUserProfileImageBtnClick() {
                //show Image pick dialog
                showImagePickDialog();
            }

            @Override
            public void editUserProfileUpdateBtnClick() {
                inputData();

            }
        });
    }
    private String name, shopName, phone, deliveryFee, country, state, city, address;
    private boolean shopOpen;

    private void inputData() {
        name = binding.nameEt.getText().toString().trim();

        phone = binding.phoneEt.getText().toString().trim();

        country = binding.countryEt.getText().toString().trim();
        state = binding.stateEt.getText().toString().trim();
        city = binding.cityEt.getText().toString().trim();
        address = binding.addressEt.getText().toString().trim();

        //validate
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            binding.nameEt.requestFocus();
            return;
        }

        if (phone.length() < 11) {
            binding.phoneEt.setError("phone number should be 11 digits");
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter phone Number...", Toast.LENGTH_SHORT).show();
            binding.phoneEt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Enter Country Name...", Toast.LENGTH_SHORT).show();
            binding.countryEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(state)) {
            Toast.makeText(this, "Enter state...", Toast.LENGTH_SHORT).show();
            binding.stateEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Enter city...", Toast.LENGTH_SHORT).show();
            binding.cityEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Enter address...", Toast.LENGTH_SHORT).show();
            binding.addressEt.requestFocus();
            return;
        }

        updateProfile();
    }

    private void updateProfile() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();
        if (image_uri == null) {
            //update without image
            //setup data to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", "" + name);

            hashMap.put("phone", "" + phone);

            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditUserActivity.this, "Profile Updated", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //update with image
            //upload image first
            String fileAndPathName = "profile_images/" + "" + firebaseAuth.getUid();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileAndPathName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ///image uploaded now get url of uploaded image
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isSuccessful());
                    Uri downloadUri = task.getResult();
                    if (task.isSuccessful()){
                        //image url is received now update this url to database

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("name", "" + name);
                        hashMap.put("phone", "" + phone);
                        hashMap.put("country", "" + country);
                        hashMap.put("state", "" + state);
                        hashMap.put("city", "" + city);
                        hashMap.put("address", "" + address);
                        hashMap.put("latitude", "" + latitude);
                        hashMap.put("longitude", "" + longitude);
                        hashMap.put("profileImage", "" + downloadUri);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileEditUserActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileEditUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }
    }
    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(ProfileEditUserActivity.this, LoginActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        //load user info and set to views
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String accountType = "" + ds.child("accountType").getValue();
                    String address = "" + ds.child("address").getValue();
                    String city = "" + ds.child("city").getValue();
                    String state = "" + ds.child("state").getValue();
                    String country = "" + ds.child("country").getValue();
                    String email = "" + ds.child("email").getValue();
                    latitude = Double.parseDouble("" + ds.child("latitude").getValue());
                    longitude = Double.parseDouble("" + ds.child("longitude").getValue());
                    String name = "" + ds.child("name").getValue();
                    String online = "" + ds.child("online").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String profileImage = "" + ds.child("profileImage").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                     String uid = "" + ds.child("uid").getValue();
                    binding.nameEt.setText(name);
                    binding.phoneEt.setText(phone);
                    binding.countryEt.setText(country);
                    binding.stateEt.setText(state);
                    binding.cityEt.setText(city);
                    binding.addressEt.setText(address);
                    try {
                        Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(binding.profileIv);
                    } catch (Exception e) {
                        binding.profileIv.setImageResource(R.drawable.ic_peson_gray);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void showImagePickDialog() {
        //option to display in dialog
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //pick from camera
                    if (checkCameraPermission()) {
                        //permission allowed open camera
                        pickFromCamera();
                    } else {
                        //not allowed ...request for camera permission
                        requestCameraPermission();
                    }
                } else {
                    //pick fro gallery
                    if (checkStoragePermission()) {
                        //permission allowed open gallery
                        pickFromGallery();
                    } else {
                        //not allowed...request for storage permission.
                        requestStoragePermission();
                    }
                }
            }
        }).create().show();

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }


    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }


    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result2;
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromGallery() {
        Intent intent=  new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);

    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please Wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        //here find address,country,state,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            //now set all info to view
            binding.addressEt.setText(address);
            binding.cityEt.setText(city);
            binding.stateEt.setText(state);
            binding.countryEt.setText(country);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Location is Disabled..", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permission allowed
                        detectLocation();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Location Permission Necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //camera and storage both permission now allowed here
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera Permissions are Necessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission now allowed here
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage Permission are Necessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //handle image pick result
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //picked from gallery
                image_uri = data.getData();
                //now set the image to imageView
                binding.profileIv.setImageURI(image_uri);
            }else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                //picked from camera
                binding.profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}