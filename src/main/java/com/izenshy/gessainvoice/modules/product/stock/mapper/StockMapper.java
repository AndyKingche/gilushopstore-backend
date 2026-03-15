package com.izenshy.gessainvoice.modules.product.stock.mapper;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.OutletModel;
import com.izenshy.gessainvoice.modules.product.product.model.ProductModel;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockDeluxeDTO;
import com.izenshy.gessainvoice.modules.product.stock.dto.StockResponseDTO;
import com.izenshy.gessainvoice.modules.product.stock.model.StockModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    // 🔹 De Entity a DTO
    @Mapping(source = "id.productId", target = "productId")
    @Mapping(source = "id.outletId", target = "outletId")
    @Mapping(source = "unit_price", target = "unitPrice")
    @Mapping(source = "pvp_price", target = "pvpPrice")
    @Mapping(source = "apply_tax", target = "applyTax")
    @Mapping(source = "ivaId.id", target = "ivaId")
    StockDTO modelToDTO(StockModel model);

    // 🔹 De DTO a Entity
    @Mapping(source = "productId", target = "id.productId")
    @Mapping(source = "outletId", target = "id.outletId")
    @Mapping(source = "unitPrice", target = "unit_price")
    @Mapping(source = "pvpPrice", target = "pvp_price")
    @Mapping(source = "applyTax", target = "apply_tax")
    @Mapping(source = "ivaId", target = "ivaId.id")
    StockModel dtoToModel(StockDTO dto);

    List<StockDTO> modelsToDTOs(List<StockModel> models);

    // 🔹 Deluxe DTO con joins
    @Mapping(source = "id.productId", target = "productId")
    @Mapping(source = "productId.productName", target = "productName")
    @Mapping(source = "productId.productCode", target = "productCode")
    @Mapping(source = "productId.productDesc", target = "productDesc")
    @Mapping(source = "productId.categoryId.categoryName", target = "categoryName")
    @Mapping(source = "productId.detailId.detailName", target = "detailName")
    @Mapping(source = "id.outletId", target = "outletId")
    @Mapping(source = "unit_price", target = "unitPrice")
    @Mapping(source = "pvp_price", target = "pvpPrice")
    @Mapping(source = "apply_tax", target = "applyTax")
    @Mapping(source = "ivaId.taxCode", target = "taxCode")
    StockDeluxeDTO modelToDeluxeDTO(StockModel model);

    List<StockDeluxeDTO> modelsToDeluxeDTOs(List<StockModel> models);

    // 🔹 Response DTO con nombre del producto
    @Mapping(source = "id.productId", target = "productId")
    @Mapping(source = "id.outletId", target = "outletId")
    @Mapping(source = "productId.productName", target = "nameProduct")
    @Mapping(source = "unit_price", target = "unitPrice")
    @Mapping(source = "pvp_price", target = "pvpPrice")
    @Mapping(source = "apply_tax", target = "applyTax")
    @Mapping(source = "ivaId.id", target = "ivaId")
    StockResponseDTO modelToResponseDTO(StockModel model);

    // Métodos auxiliares para mapear entidades por id si los necesitas
    default ProductModel map(Long value) {
        if (value == null) return null;
        ProductModel product = new ProductModel();
        product.setId(value);
        return product;
    }

    default OutletModel mapOutlet(Long value) {
        if (value == null) return null;
        OutletModel outlet = new OutletModel();
        outlet.setOutletId(value);
        return outlet;
    }

    default Long map(ProductModel product) {
        return product != null ? product.getId() : null;
    }

    default Long map(OutletModel outlet) {
        return outlet != null ? outlet.getOutletId() : null;
    }
}
