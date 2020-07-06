package com.example.groceryapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    public String email;
    public String password;
    public String button;
    public String resister;
}
