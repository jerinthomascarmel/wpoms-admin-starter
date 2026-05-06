package com.wpoms.admin.models.payloads;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptOrderPayload {

    @NotNull(message = "Delivery date is required")
    private String deliveryDate;
}
