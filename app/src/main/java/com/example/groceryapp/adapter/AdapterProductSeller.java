package com.example.groceryapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryapp.R;
import com.example.groceryapp.filter.FilterProducts;
import com.example.groceryapp.model.ModelProduct;
import com.example.groceryapp.view.EditProductActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {
    Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProducts filter;
    String image_path;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller, parent, false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        final ModelProduct modelProduct = productsList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        image_path = icon;
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();
        //now set data to view
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountNoteTv.setText(discountNote + "%" + " OFF");
        holder.discountedPriceTv.setText("$" + discountPrice);
        holder.originalPriceTv.setText("$" + originalPrice);
        if (discountAvailable.equals("true")) {
            //product is on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        } else {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(holder.productIconIv);
        } catch (Exception e) {
            holder.productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks show item details...(in Buttom sheet)
                detailsBottomSheet(modelProduct);//here model products contains details of clicked product

            }
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //Inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller, null);
        //set view to bottom sheet
        bottomSheetDialog.setContentView(view);
        //init view of bottom sheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        //get data
        final String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        final String title = modelProduct.getProductTitle();
        String timeStamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();
        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountNoteTv.setText(discountNote + "%" + " OFF");
        discountedPriceTv.setText("$" + discountPrice);
        originalPriceTv.setText("$" + originalPrice);

        if (discountAvailable.equals("true")) {
            //product is on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//add strike through on original price
        } else {
            //product is not on discount
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_shopping_primary).into(productIconIv);
        } catch (Exception e) {
            productIconIv.setImageResource(R.drawable.ic_add_shopping_primary);
        }
        bottomSheetDialog.show();
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);//this is clicked product id
                context.startActivity(intent);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete").setMessage("Are you Sure want to delete product " + title + " ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteProduct(id);//here id is a product id
                                bottomSheetDialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String id) {//delete product using product id...
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //product deleted
                //now delete database storage image
                StorageReference reference1 = FirebaseStorage.getInstance().getReferenceFromUrl(image_path);//this is the image url..here image url is image path
                reference1.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //product image deleted...
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //product delete failed
                Toast.makeText(context, "Product delete failed : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {//for search a product after make a filter class called filter products then implement Filterable
        if (filter == null) {
            filter = new FilterProducts(this, filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder {
        private ImageView productIconIv;
        private TextView discountNoteTv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;


        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);
            productIconIv = itemView.findViewById(R.id.productIconIv);
            discountNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);
        }
    }
}
