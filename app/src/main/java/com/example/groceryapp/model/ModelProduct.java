package com.example.groceryapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelProduct {
    private String productId,productTitle,productDescription,productCategory,productQuantity,productIcon,originalPrice,discountPrice,
            discountNote,discountAvailable,timestamp,uid;
}
