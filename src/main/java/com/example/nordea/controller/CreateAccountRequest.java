package com.example.nordea.controller;

import com.example.nordea.model.ProductType;
import com.example.nordea.model.TaxCountry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String holderName;

    @NotNull
    private TaxCountry taxCountry;

    @NotNull
    private ProductType productType;
}
