package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
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
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.databinding.ActivityResisterSellerBinding;

import java.util.List;
import java.util.Locale;

public class ResisterSellerActivity extends AppCompatActivity implements LocationListener {
ActivityResisterSellerBinding activityResisterSellerBinding;
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
    private double latitude,longitude;
    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_resister_seller);
        activityResisterSellerBinding = DataBindingUtil.setContentView(this,R.layout.activity_resister_seller);
        //init permission arrays
        locationPermissions =  new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions =  new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions =  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        activityResisterSellerBinding.setClickHandle(new com.example.groceryapp.commands.ResisterSellerActivity() {
            @Override
            public void sellerBackBtnClick() {
                onBackPressed();
            }

            @Override
            public void sellerGpsBtnClick() {
                //detect current location
                if (checkLocationPermission()){
                    //already allowed so
                    detectLocation();
                }else {
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
            }
        });
    }

    private void showImagePickDialog() {
        //option to display dialog
        String option[] = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                //camera permission allowed
                                pickFromCamera();

                            }else {
                                //now allowed ,,,so request
                                requestCameraPermission();
                            }
                        }else {
                            //gallery clicked
                            //camera clicked
                            if (checkStoragePermission()){
                                //storage permission allowed
                                pickedFromGallery();
                            }else {
                                //not allowed ,, so request
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }
    private void pickedFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_Image Tile");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void detectLocation() {
        Toast.makeText(this, "Please Wait....", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }
    private void findAddress()  {
        //find address ,country,state city
        Geocoder geocoder;
        List<Address>addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);//complete Address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            //set Address
            activityResisterSellerBinding.countryEt.setText(country);
            activityResisterSellerBinding.stateEt.setText(state);
            activityResisterSellerBinding.cityEt.setText(city);
            activityResisterSellerBinding.addressEt.setText(address);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
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
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation();
                    }else {
                        //permission denied
                        Toast.makeText(this, "Location Permission is Necessary...", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //permission allowed
                        pickFromCamera();
                    }else {
                        //permission denied
                        Toast.makeText(this, "Camera Permissions are Necessary...", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        //permission allowed
                        pickedFromGallery();
                    }else {
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
        if (requestCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){
                //get picked image
                image_uri = data.getData();
                //set to imageView
                activityResisterSellerBinding.profileIv.setImageURI(image_uri);
            }else if (requestCode==IMAGE_PICK_CAMERA_CODE){
                //set to imageView
                activityResisterSellerBinding.profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}