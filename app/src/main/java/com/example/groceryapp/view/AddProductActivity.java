package com.example.groceryapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.groceryapp.R;
import com.example.groceryapp.commands.AddProductActivityClicks;
import com.example.groceryapp.databinding.ActivityAddProductBinding;
import com.example.groceryapp.util.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddProductActivity extends AppCompatActivity {
    ActivityAddProductBinding binding;
    private Uri image_uri;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    //permission Constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //IMAGE PICK CONSTANTS
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //PERMISSION ARRAYS..
    private String cameraPermission[];
    private String storagePermission[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add_product);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_product);
        //unchecked   show hide discount price Et discount note Et.....
        binding.discountedPriceEt.setVisibility(View.GONE);
        binding.discountedNoteEt.setVisibility(View.GONE);
        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        //inti permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        binding.setClickHandle(new AddProductActivityClicks() {
            @Override
            public void addProductBtn() {//for add product
                //1)input data
                //2)validate data
                //3)add data to database
                inputData();
            }

            @Override
            public void backBtnClick() {
                onBackPressed();
            }

            @Override
            public void productIconClick() {
                //will add validation
                showImagePickDialog();
            }

            @Override
            public void categoryTvClick() {
                //pic category
                CategoryDialog();
            }
        });
        binding.discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //checked,,show discount price Et discount note Et....
                    binding.discountedPriceEt.setVisibility(View.VISIBLE);
                    binding.discountedNoteEt.setVisibility(View.VISIBLE);

                } else {
                    //unchecked   show hide discount price Et discount note Et.....
                    binding.discountedPriceEt.setVisibility(View.GONE);
                    binding.discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });
    }

    private String productTitle, description, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        productTitle = binding.titleEt.getText().toString().trim();

        description = binding.descriptionEt.getText().toString().trim();

        productCategory = binding.categoryTv.getText().toString().trim();
        productQuantity = binding.quantityEt.getText().toString().trim();
        originalPrice = binding.priceEt.getText().toString().trim();
        discountPrice = binding.discountedPriceEt.getText().toString().trim();
        discountNote = binding.discountedNoteEt.getText().toString().trim();
        discountAvailable = binding.discountSwitch.isChecked();//get true or false
        //validate
        if (TextUtils.isEmpty(productTitle)) {
            binding.titleEt.setError("Title is Required");
            binding.titleEt.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(productCategory)) {
            binding.categoryTv.setError("Category is Required");
            binding.categoryTv.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)) {
            binding.priceEt.setError("Original Price is Required");
            binding.priceEt.requestFocus();
            return;
        }
        if (discountAvailable) {
            //product is with discount
            discountPrice = binding.discountedPriceEt.getText().toString().trim();
            discountNote = binding.discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)) {
                binding.discountedPriceEt.setError("Discount  Price is Required");
                binding.discountedPriceEt.requestFocus();
                return;
            }
        } else {
            //product without discount
            discountPrice = "0";
            discountNote = "";
        }
        addProduct();
    }

    private void addProduct() {
        progressDialog.setMessage("Adding Product");
        progressDialog.show();
        final String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            //upload Without image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + description);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", "" + "");//no image
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());
            //now add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product added...", Toast.LENGTH_LONG).show();
                            clearData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            //upload with image
            //first upload image to firebase storage
            //name and path image to be uploaded
            String filePathAndName = "product_images/" + "" + timestamp;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded ..now get url the uploaded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadImageUri = uriTask.getResult();
                    if (uriTask.isSuccessful()){
                        //url of image received now upload the url to db
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("productId", "" + timestamp);
                        hashMap.put("productTitle", "" + productTitle);
                        hashMap.put("productDescription", "" + description);
                        hashMap.put("productCategory", "" + productCategory);
                        hashMap.put("productQuantity", "" + productQuantity);
                        hashMap.put("productIcon", "" + downloadImageUri);
                        hashMap.put("originalPrice", "" + originalPrice);
                        hashMap.put("discountPrice", "" + discountPrice);
                        hashMap.put("discountNote", "" + discountNote);
                        hashMap.put("discountAvailable", "" + discountAvailable);
                        hashMap.put("timestamp", "" + timestamp);
                        hashMap.put("uid", "" + firebaseAuth.getUid());
                        //now add to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddProductActivity.this, "Product added...", Toast.LENGTH_LONG).show();
                                        clearData();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(AddProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void clearData() {
        //clear data after uploading product
        binding.titleEt.setText("");
        binding.descriptionEt.setText("");
        binding.categoryTv.setText("");
        binding.quantityEt.setText("");
        binding.priceEt.setText("");
        binding.discountedPriceEt.setText("");
        binding.discountedNoteEt.setText("");
        binding.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        image_uri = null;
    }

    private void CategoryDialog() {
        //for get category
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.PRODUCT_CATEGORY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String category = Constants.PRODUCT_CATEGORY[which];//get picked categories..
                binding.categoryTv.setText(category);
            }
        }).create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
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
                binding.productIconIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //picked from camera
                binding.productIconIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}