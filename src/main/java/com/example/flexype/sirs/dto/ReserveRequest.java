package com.example.flexype.sirs.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRequest {

    private Long userId;
    private String sku;
    private int quantity = 1;
}
