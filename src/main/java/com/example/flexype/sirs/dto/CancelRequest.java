package com.example.flexype.sirs.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {
    private String reservationId;
    private Long userId;
}
