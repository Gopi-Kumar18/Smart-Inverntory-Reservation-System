package com.example.flexype.sirs.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmRequest {
    private String reservationId;
    private Long userId;
    // add payment fields here later if needed (e.g., paymentId)
}
