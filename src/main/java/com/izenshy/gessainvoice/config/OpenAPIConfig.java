package com.izenshy.gessainvoice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(info = @Info(title = "GESSA INVOICE APP", version = "1.0",
        description = "Esta API REST permite gestionar la facturacion electronica con los servicios del SRI",
        contact = @Contact(name = "Autor: Andykingche", email = "laqm_14@hotmail.com",
                extensions = {@Extension(name = "x-github", properties = {
                        @ExtensionProperty(name = "url", value = "https://github.com/AndyKingche")
                })})),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(
                        url = "/",
                        description = "Gessa Invoice API"
                )
        },
        security = {@SecurityRequirement(name = "bearerToken")}
)
@SecuritySchemes({
        @SecurityScheme(name = "bearerToken", type = SecuritySchemeType.HTTP,
                scheme = "bearer", bearerFormat = "JWT")
})
public class OpenAPIConfig {

}
