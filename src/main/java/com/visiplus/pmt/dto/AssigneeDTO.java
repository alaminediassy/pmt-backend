package com.visiplus.pmt.dto;

import lombok.Data;

@Data
public class AssigneeDTO {
    private Long id;
    private String username;
    private String email;

    public AssigneeDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
