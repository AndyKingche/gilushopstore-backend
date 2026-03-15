package com.izenshy.gessainvoice.controller;

import com.izenshy.gessainvoice.common.response.GessaApiResponse;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationAuxResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationRequestDTO;
import com.izenshy.gessainvoice.modules.invoice.dto.InvoiceTempAuthorizationResponseDTO;
import com.izenshy.gessainvoice.modules.invoice.service.InvoiceTempAuthorizationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gessa/invoice-temp-authorization")
@Tag(name = "Invoice Temp Authorization", description = "Esta sección es dedicada a las operaciones relacionadas con la autorización temporal de facturas del sistema")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class InvoiceTempAuthorizationController {

    private final InvoiceTempAuthorizationService service;

    @Autowired
    public InvoiceTempAuthorizationController(InvoiceTempAuthorizationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GessaApiResponse<InvoiceTempAuthorizationResponseDTO>> create(
            @RequestBody InvoiceTempAuthorizationRequestDTO requestDTO) {
        try {
            var model = service.saveFromDTO(requestDTO);
            var responseDTO = service.toResponseDTO(model);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(GessaApiResponse.success("Invoice temp authorization created successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error creating invoice temp authorization: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceTempAuthorizationResponseDTO>> update(
            @PathVariable Long id,
            @RequestBody InvoiceTempAuthorizationRequestDTO requestDTO) {
        try {
            var model = service.update(id, requestDTO);
            var responseDTO = service.toResponseDTO(model);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice temp authorization updated successfully", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error updating invoice temp authorization: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GessaApiResponse<InvoiceTempAuthorizationResponseDTO>> getById(@PathVariable Long id) {
        try {
            var model = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invoice temp authorization not found with id: " + id));
            var responseDTO = service.toResponseDTO(model);
            return ResponseEntity.ok(GessaApiResponse.success("invoice id", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorization: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationResponseDTO>>> getAll() {
        try {
            var models = service.findAll();
            var responseDTOs = service.toResponseDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("List Completa", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GessaApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(GessaApiResponse.success("Invoice temp authorization deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error deleting invoice temp authorization: " + e.getMessage()));
        }
    }

    @GetMapping("/uuid/{tempUuid}")
    public ResponseEntity<GessaApiResponse<InvoiceTempAuthorizationResponseDTO>> getByTempUuid(
            @PathVariable UUID tempUuid) {
        try {
            var model = service.findByTempUuid(tempUuid)
                    .orElseThrow(() -> new RuntimeException("Invoice temp authorization not found with uuid: " + tempUuid));
            var responseDTO = service.toResponseDTO(model);
            return ResponseEntity.ok(GessaApiResponse.success("Uuuid", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorization: " + e.getMessage()));
        }
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationAuxResponseDTO>>> getByEnterpriseId(
            @PathVariable Long enterpriseId) {
        try {
            var models = service.findByEnterpriseId(enterpriseId);
            var responseDTOs = service.toResponseAuxDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @GetMapping("/outlet/{outletId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationResponseDTO>>> getByOutletId(
            @PathVariable Long outletId) {
        try {
            var models = service.findByOutletId(outletId);
            var responseDTOs = service.toResponseDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Outlet",responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationResponseDTO>>> getByInvoiceId(
            @PathVariable Long invoiceId) {
        try {
            var models = service.findByInvoiceId(invoiceId);
            var responseDTOs = service.toResponseDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa",responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @GetMapping("/list-all/{enterpriseId}/{outletId}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationAuxResponseDTO>>> getByInvoiceIdAndEnterpriseIdAndOutletId(
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId
            ) {
        try {
            var models = service.findByEnterpriseId_IdAndOutletId_OutletId(enterpriseId,outletId);
            var responseDTOs = service.toResponseAuxDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa",responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @GetMapping("/access-code/{accessCode}")
    public ResponseEntity<GessaApiResponse<InvoiceTempAuthorizationResponseDTO>> getByAccessCode(
            @PathVariable String accessCode) {
        try {
            var model = service.findByAccessCode(accessCode)
                    .orElseThrow(() -> new RuntimeException("Invoice temp authorization not found with access code: " + accessCode));
            var responseDTO = service.toResponseDTO(model);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa", responseDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorization: " + e.getMessage()));
        }
    }

    @GetMapping("/reception-status/{receptionStatus}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationResponseDTO>>> getByReceptionStatus(
            @PathVariable String receptionStatus) {
        try {
            var models = service.findByReceptionStatus(receptionStatus);
            var responseDTOs = service.toResponseDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }

    @GetMapping("/authorization-status/{authorizationStatus}")
    public ResponseEntity<GessaApiResponse<List<InvoiceTempAuthorizationResponseDTO>>> getByAuthorizationStatus(
            @PathVariable String authorizationStatus) {
        try {
            var models = service.findByAuthorizationStatus(authorizationStatus);
            var responseDTOs = service.toResponseDTOList(models);
            return ResponseEntity.ok(GessaApiResponse.success("Lista Completa", responseDTOs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }

    }

    @GetMapping("/resend-billing/{invoiceId}/{enterpriseId}/{outletId}")
    public ResponseEntity<GessaApiResponse<?>> reSendBilling(
            @PathVariable Long invoiceId,
            @PathVariable Long enterpriseId,
            @PathVariable Long outletId
    ) {
        try {
            boolean models = service.reSendBillingSRI(invoiceId, enterpriseId, outletId);
            GessaApiResponse result = new GessaApiResponse();

            if(models){
                result.setMessage("Se realizo el envio correctamente");
                result.setData(null);
                result.setSuccess(true);
                return ResponseEntity.ok(result);

            }else{
                result.setMessage("No se realizo el envio, vuelva a intentar mas tarde");
                result.setData(null);
                result.setSuccess(false);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(GessaApiResponse.error("Error getting invoice temp authorizations: " + e.getMessage()));
        }
    }
}
