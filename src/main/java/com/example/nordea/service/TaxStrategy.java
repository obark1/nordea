package com.example.nordea.service;

import com.example.nordea.model.TaxCountry;
import com.example.nordea.model.TaxInput;
import com.example.nordea.model.TaxResult;

public interface TaxStrategy {
    TaxResult calculate(TaxInput taxInput);

    TaxCountry supportedCountry();
}
