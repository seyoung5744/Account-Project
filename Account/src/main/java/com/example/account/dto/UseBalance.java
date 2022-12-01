package com.example.account.dto;

import com.example.account.aop.AccountLockIdInterface;
import com.example.account.type.TransactionResultType;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class UseBalance {
    /**
     * {
     * "userId":1,
     * "accountNumber":"1000000000",
     * "amount":1000
     * }
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class Request implements AccountLockIdInterface {
        @NotNull
        @Min(1)
        private Long userId;

        @NotNull
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        /**
         * {
         * "accountNumber":"1234567890",
         * "transactionResult":"S",
         * "transactionId":"c2033bb6d82a4250aecf8e27c49b63f6",
         * "amount":1000,
         * "transactedAt":"2022-06-01T23:26:14.671859"
         * }
         */
        private String accountNumber; // 계좌번호
        private TransactionResultType transactionResult; // 거래 결과 (성공, 실패)
        private String transactionId; // 거래 아이디
        private Long amount; // 거래 금액
        private LocalDateTime transactedAt; // 거래일시

        public static Response from(TransactionDto transactionDto) {
            return Response.builder()
                    .accountNumber(transactionDto.getAccountNumber())
                    .transactionResult(transactionDto.getTransactionResultType())
                    .transactionId(transactionDto.getTransactionId())
                    .amount(transactionDto.getAmount())
                    .transactedAt(transactionDto.getTransactedAt())
                    .build();
        }
    }

}
