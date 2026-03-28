package com.izenshy.gessainvoice.modules.product.stock.model;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

@Entity
@Table(name="stock")
@NamedQuery(name = "StockModel.findAll", query = "SELECT stock FROM StockModel stock")
@Data
public class StockModel implements Serializable {
    @EmbeddedId
    private StockPKModel id = new StockPKModel();

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name="stock_product_id")
    private ProductModel productId;

    @Column(name = "stock_quantity")
    private float stockQuantity;

    @Column(name = "stock_avalible")
    private Boolean stockAvalible;

    @Column(name="unit_price")
    private double unit_price;

    @Column(name="pvp_price")
    private double pvp_price;

    @Column(name="stock_max")
    private int stockMax;

    @Column(name="stock_min")
    private int stockMin;

    @Column(name = "apply_tax")
    private Boolean apply_tax;

    @ManyToOne
    @JoinColumn(name = "tax_id")
    private TaxModel ivaId;

    @ManyToOne
    @MapsId("outletId")
    @JoinColumn(name="stock_outlet_id")
    private OutletModel outletId;
}
