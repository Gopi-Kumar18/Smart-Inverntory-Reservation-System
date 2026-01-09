package com.example.flexype.sirs.controller;

import com.example.flexype.sirs.dto.InventoryResponse;
import com.example.flexype.sirs.service.InventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventory;

    public InventoryController(InventoryService inventory) { this.inventory = inventory; }

    @GetMapping("/{sku}")
    public InventoryResponse getInventory(@PathVariable String sku) {
        long available = inventory.getAvailable(sku);
        InventoryResponse resp = new InventoryResponse();
        resp.setSku(sku);
        resp.setAvailable(available);
        return resp;
    }
}
