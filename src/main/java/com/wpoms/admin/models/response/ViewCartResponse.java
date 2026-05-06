package com.wpoms.admin.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewCartResponse {
    private int cartId;
    private List<CartItemDetail> items;
    private double totalAmount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItemDetail {
        private int cartItemId;
        private int productId;
        private String productName;
        private String manufacturer;
        private double price;
        private int quantity;
        private double subtotal;
    }
}


