package com.izenshy.gessainvoice.modules.product.product.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class TaxDTO implements Serializable {
    private String taxCode;
    private String taxPercentage;
    private Integer codeSri;
    private Float taxValue;
}
