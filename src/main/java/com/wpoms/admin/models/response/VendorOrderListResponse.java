package com.wpoms.admin.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorOrderListResponse {
    private List<VendorOrderSummary> orders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VendorOrderSummary {
        private String poNumber;
        private String manufacturer;
        private String orderDate;
        private String status;
        private String deliveryDate;
    }
}
