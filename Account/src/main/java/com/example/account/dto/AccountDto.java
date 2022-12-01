package com.example.account.dto;


import com.example.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

// AccountDto는 Entity 클래스와 비슷하지만 좀 더 단순화하여 사용한다.
// Controller와 Service간 통신을 위한 Dto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    // 응답에 필요한 데이터 정의
    private Long userId;
    private String accountNumber;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    // Entity를 Dto로 변환해주기 위한 static Method
    // 굳이 static으로 만드는 이유
    // 1. 생성자를 이용하면 가독성에 좋지 않음. 파라미터가 많아지면 한도 끝도 없다.
    // 2. 생성자 이용시 값 순서까지 신경써야 한다.
    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .registeredAt(account.getRegisteredAt())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }

}
