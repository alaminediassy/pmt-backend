package com.visiplus.pmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InviteRequestDTO {
    private String email;

    public InviteRequestDTO(String email) {
        this.email = email;
    }
}
