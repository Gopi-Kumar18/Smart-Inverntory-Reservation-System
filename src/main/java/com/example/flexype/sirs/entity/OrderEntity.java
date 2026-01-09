package com.example.flexype.sirs.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(columnNames = "reservationId")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reservationId;

    private String sku;

    private Long userId;

    private int quantity;

    private double price;

    private Instant createdAt;
}

