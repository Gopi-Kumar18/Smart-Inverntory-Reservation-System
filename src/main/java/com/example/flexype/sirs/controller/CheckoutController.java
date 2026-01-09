package com.example.flexype.sirs.controller;


import com.example.flexype.sirs.dto.*;
import com.example.flexype.sirs.service.ReservationService;
import com.example.flexype.sirs.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class CheckoutController {
    private final ReservationService reservationService;
    private final OrderService orderService;

    public CheckoutController(ReservationService reservationService, OrderService orderService) {
        this.reservationService = reservationService;
        this.orderService = orderService;
    }

    @PostMapping("/inventory/reserve")
    public ReserveResponse reserve(@RequestBody ReserveRequest req) {
        return reservationService.reserve(req);
    }

    @PostMapping("/checkout/confirm")
    public String confirm(@RequestBody ConfirmRequest req) {
        return orderService.confirm(req);
    }

    @PostMapping("/checkout/cancel")
    public String cancel(@RequestBody CancelRequest req) {
        return orderService.cancel(req);
    }
}
