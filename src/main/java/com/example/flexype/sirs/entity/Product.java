package com.example.flexype.sirs.entity;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String sku;

    private String name;

    private long initialStock;

    private double price;
}

