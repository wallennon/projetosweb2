package br.edu.unifip.ecommerceapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private BigDecimal price;
    @NotNull
    private UUID category;

    public UUID getCategory() {
        return category;
    }
}
