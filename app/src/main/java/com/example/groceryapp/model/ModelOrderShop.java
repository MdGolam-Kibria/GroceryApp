package com.example.groceryapp.model;

import lombok.Data;

@Data
public class ModelOrderShop {
private String orderId,orderTime,orderStatus,orderCost,deliveryFee,orderBy,
        orderTo,latitude,longitude;
}
