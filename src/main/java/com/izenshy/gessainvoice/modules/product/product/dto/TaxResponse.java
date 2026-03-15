package com.izenshy.gessainvoice.modules.product.product.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaxResponse {

    private Long id;
    private String taxCode;
    private String taxPercentage;
    private Integer codeSri;
    private Float taxValue;

}
