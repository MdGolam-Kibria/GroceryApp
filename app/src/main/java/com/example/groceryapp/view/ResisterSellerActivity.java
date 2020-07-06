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
import android.util.Patterns;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityResisterSellerBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ResisterSellerActivity extends AppCompatActivity implements LocationListener {
    ActivityResisterSellerBinding binding;
    //permission constants
    public static final int LOCATION_REQUEST_CODE = 100;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //PERMISSION ARRAYS
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked Uri
    private Uri image_uri;
    private double latitude, longitude;
    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_resister_seller);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_resister_seller);
        //init permission arrays
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        binding.setClickHandle(new com.example.groceryapp.commands.ResisterSellerActivity() {
            @Override
            public void sellerBackBtnClick() {
                onBackPressed();
            }

            @Override
            public void sellerGpsBtnClick() {
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed so
                    detectLocation();
                } else {
                    //not allowed request for location permission
                    requestLocationPermission();
                }
            }

            @Override
            public void sellerProfileIvClick() {
                //pick image
                showImagePickDialog();
            }

            @Override
            public void sellerResisterBtnClick() {
                //resister seller
                inputData();
            }
        });
    }

    String fullName, shopName, phoneNumber, deliveryFee, country, state, city, address, email, password, confirmPassword;

    private void inputData() {
        fullName = binding.nameEt.getText().toString().trim();
        shopName = binding.shopNameEt.getText().toString().trim();
        phoneNumber = binding.phoneEt.getText().toString().trim();
        deliveryFee = binding.deliveryFeeEt.getText().toString().trim();
        country = binding.countryEt.getText().toString().trim();
        state = binding.stateEt.getText().toString().trim();
        city = binding.cityEt.getText().toString().trim();
        address = binding.addressEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        confirmPassword = binding.cPasswordEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            binding.nameEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Enter Shop Name...", Toast.LENGTH_SHORT).show();
            binding.shopNameEt.requestFocus();
            return;
        }
        if (phoneNumber.length() < 11) {
            binding.phoneEt.setError("phone number should be 11 digits");
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter phone Number...", Toast.LENGTH_SHORT).show();
            binding.phoneEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(deliveryFee)) {
            Toast.makeText(this, "Enter deliveryFee...", Toast.LENGTH_SHORT).show();
            binding.deliveryFeeEt.requestFocus();
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
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter Email...", Toast.LENGTH_SHORT).show();
            binding.emailEt.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid Email Address");
            binding.emailEt.requestFocus();
            return;
        }
        if (password.length() < 6) {
            binding.passwordEt.setError("password length should be 6 or above");
            binding.passwordEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter Password...", Toast.LENGTH_SHORT).show();
            binding.passwordEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Enter confirm password...", Toast.LENGTH_SHORT).show();
            binding.cPasswordEt.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "don't match password", Toast.LENGTH_SHORT).show();
            binding.passwordEt.setError("password & confirm password should be same");
            binding.cPasswordEt.setError("password & confirm password should be same");
            binding.passwordEt.requestFocus();
            binding.cPasswordEt.requestFocus();
        }

        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating account...");
        progressDialog.show();
        //create account
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                saveFirebaseData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ResisterSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFirebaseData() {
        progressDialog.setMessage("saving account info..");
        final String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            //save Info Without Image
            //setUp data to save
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + fullName);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("deliveryFee", "" + deliveryFee);
            hashMap.put("country", "" + country);
            hashMap.put("state", "" + state);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("accountType", "" + "Seller");
            hashMap.put("online", "" + "true");
            hashMap.put("shopOpen", "" + "true");
            hashMap.put("profileImage", "" + "");
            //save data to database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //database updated
                    progressDialog.dismiss();
                    startActivity(new Intent(ResisterSellerActivity.this, MainSellerActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed database update
                    progressDialog.dismiss();
                    startActivity(new Intent(ResisterSellerActivity.this, MainSellerActivity.class));
                    finish();
                }
            });


        } else {
            //save Info With Image

            //name and path of image
            String fileAndPathName = "profile_images/" + "" + firebaseAuth.getUid();
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileAndPathName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //get url of uploded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUri = uriTask.getResult();
                    if (uriTask.isSuccessful()) {
                        //setUp data to save
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("uid", "" + firebaseAuth.getUid());
                        hashMap.put("email", "" + email);
                        hashMap.put("name", "" + fullName);
                        hashMap.put("shopName", "" + shopName);
                        hashMap.put("phone", "" + phoneNumber);
                        hashMap.put("deliveryFee", "" + deliveryFee);
                        hashMap.put("country", "" + country);
                        hashMap.put("state", "" + state);
                        hashMap.put("city", "" + city);
                        hashMap.put("address", "" + address);
                        hashMap.put("latitude", "" + latitude);
                        hashMap.put("longitude", "" + longitude);
                        hashMap.put("timestamp", "" + timestamp);
                        hashMap.put("accountType", "" + "Seller");
                        hashMap.put("online", "" + "true");
                        hashMap.put("shopOpen", "" + "true");
                        hashMap.put("profileImage", "" + downloadUri);//url of uploaded image
                        //save data to database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //database updated
                                progressDialog.dismiss();
                                startActivity(new Intent(ResisterSellerActivity.this, MainSellerActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //failed database update
                                progressDialog.dismiss();
                                startActivity(new Intent(ResisterSellerActivity.this, MainSellerActivity.class));
                                finish();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ResisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void showImagePickDialog() {
        //option to display dialog
        String option[] = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //camera permission allowed
                                pickFromCamera();

                            } else {
                                //now allowed ,,,so request
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            //camera clicked
                            if (checkStoragePermission()) {
                                //storage permission allowed
                                pickedFromGallery();
                            } else {
                                //not allowed ,, so request
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }

    private void pickedFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Tile");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please Wait....", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void findAddress() {
        //find address ,country,state city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = addresses.get(0).getAddressLine(0);//complete Address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            //set Address
            binding.countryEt.setText(country);
            binding.stateEt.setText(state);
            binding.cityEt.setText(city);
            binding.addressEt.setText(address);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(Location location) {
        //location detected
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
        //gps/location disabled
        Toast.makeText(this, "Please Turn On Location.....", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Location Permission is Necessary...", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera Permissions are Necessary...", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickedFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage Permission is Necessary...", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                image_uri = data.getData();
                //set to imageView
                binding.profileIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageView
                image_uri = data.getData();
                binding.profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}