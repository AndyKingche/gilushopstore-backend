package com.izenshy.gessainvoice.modules.product.product.mapper;

import com.izenshy.gessainvoice.modules.product.product.dto.TaxDTO;
import com.izenshy.gessainvoice.modules.product.product.dto.TaxResponse;
import com.izenshy.gessainvoice.modules.product.product.model.TaxModel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxMapper {
    TaxModel dtoToModel(TaxDTO dto);
    TaxDTO modelToDTO(TaxModel model);
    TaxResponse modelToResponse(TaxModel model);
    List<TaxDTO> modelsToDTOs(List<TaxModel> models);
    List<TaxResponse> modelsToResponses(List<TaxModel> models);
}
