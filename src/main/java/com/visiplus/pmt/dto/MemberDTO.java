package com.visiplus.pmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDTO {
    private Long id;
    private String fullName;
    private String email;
}
