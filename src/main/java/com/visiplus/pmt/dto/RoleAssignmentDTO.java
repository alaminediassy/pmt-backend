package com.visiplus.pmt.dto;

import com.visiplus.pmt.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleAssignmentDTO {
    private Role role;

    public RoleAssignmentDTO(Role role) {
        this.role = role;
    }
}
