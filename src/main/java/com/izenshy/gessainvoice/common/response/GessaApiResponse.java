package com.izenshy.gessainvoice.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GessaApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> GessaApiResponse<T> success(String message, T data) {
        return new GessaApiResponse<>(true, message, data);
    }


    public static <T> GessaApiResponse<T> error(String message) {
        return new GessaApiResponse<>(false, message, null);
    }
}
