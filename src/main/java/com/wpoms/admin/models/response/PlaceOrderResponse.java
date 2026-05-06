package com.wpoms.admin.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {
    private String message;
    private List<OrderSummary> orders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderSummary {
        private String poNumber;
        private String manufacturer;
        private String status;
        private double totalAmount;
    }
}
