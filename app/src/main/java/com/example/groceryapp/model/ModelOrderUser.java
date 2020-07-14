package com.example.groceryapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelOrderUser {
    String orderId, orderTime, orderStatus, orderCost, orderBy, orderTo;
}
