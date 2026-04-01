package com.vbforge.libraryapi.dto.request;

import com.vbforge.libraryapi.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateRoleRequest {

    @NotNull(message = "Role must not be null")
    private Role role;

}
