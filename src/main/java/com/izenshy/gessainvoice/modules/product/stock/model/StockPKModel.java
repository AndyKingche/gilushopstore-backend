package com.izenshy.gessainvoice.modules.product.stock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class StockPKModel implements Serializable {

    @Column(name="stock_product_id", insertable=false, updatable=false)
    private Long productId;

    @Column(name="stock_outlet_id", insertable=false, updatable=false)
    private Long outletId;

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StockPKModel)) {
            return false;
        }
        StockPKModel castOther = (StockPKModel)other;
        return
                (this.productId == castOther.productId)
                        && (this.outletId == castOther.outletId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash =  17;
        hash = (int) (hash * prime + this.productId);
        hash = (int) (hash * prime + this.outletId);

        return hash;
    }
}
