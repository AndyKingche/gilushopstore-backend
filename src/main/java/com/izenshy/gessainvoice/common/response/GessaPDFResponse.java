package com.izenshy.gessainvoice.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GessaPDFResponse<T> {

    private boolean type;
    private String message;
    private Long invoiceId;
    private byte[] data;

    public static <T> GessaPDFResponse<T> success(String message, Long invoiceId,byte[] data) {
        return new GessaPDFResponse<>(true, message, invoiceId, data);
    }


    public static <T> GessaPDFResponse<T> error(String message) {
        return new GessaPDFResponse<>(false, message, null,null);
    }
}
