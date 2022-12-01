package com.example.account.dto;


import lombok.*;

// Client와 application 간 응답 Dto
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountInfo {
    private String accountNumber;
    private Long balance;
}
