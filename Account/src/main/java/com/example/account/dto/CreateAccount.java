package com.example.account.dto;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAccount {
    // CreateAccontRequest 혹은 Response 따로 클래스를 만들 수도 있지만 내부 클래스 방식을 사용해서 좀 더 명시적으로 알아보기 좋게 만들 수도 있음.

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        // AccountController @Valid를 위한 조건 추가
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Min(0)
        private Long initialBalance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId; // 사용자 아이디
        private String accountNumber; // 계좌 번호
        private LocalDateTime registeredAt; // 계좌 등록 일시

        public static Response from(AccountDto accountDto) {
            return Response.builder()
                    .userId(accountDto.getUserId())
                    .accountNumber(accountDto.getAccountNumber())
                    .registeredAt(accountDto.getRegisteredAt())
                    .build();
        }
    }
}
