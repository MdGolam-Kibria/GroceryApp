package com.example.groceryapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelCartItem {
    String id,pId,name,price,cost,quantity;
}
