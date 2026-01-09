package com.example.flexype.sirs.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveResponse {

    private String reservationId;
    private String sku;
    private int quantity;
    private long expiresInSeconds;
    private String message;
}
