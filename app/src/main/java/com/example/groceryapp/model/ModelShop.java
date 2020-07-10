package com.example.groceryapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelShop {
String uid,email,name,shopName,phone,deliveryFee,country,state,city,address,
        latitude,longitude, timestamp,accountType,online,shopOpen,profileImage;

}
