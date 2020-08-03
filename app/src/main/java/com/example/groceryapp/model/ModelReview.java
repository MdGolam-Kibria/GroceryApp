package com.example.groceryapp.model;

import lombok.Data;

@Data
public class ModelReview {
    //use same spelling of variables  as used in sending firebase
    private String uid, ratings, review, timestamp;
}
