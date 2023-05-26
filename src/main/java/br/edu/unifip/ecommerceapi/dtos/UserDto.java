package br.edu.unifip.ecommerceapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class UserDto {
    @NotBlank
    private String name;
    @NotNull
    private int age;
//    @NotNull
//    private UUID category;
//
//    public UUID getCategory() {
//        return category;
//    }
}
