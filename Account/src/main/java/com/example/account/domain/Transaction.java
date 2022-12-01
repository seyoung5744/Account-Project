package com.example.account.domain;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Transaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    @ManyToOne
    private Account account;

    private Long amount;

    private Long balanceSnapshot;

    private String transactionId; // PK를 그대로 쓰면 보안상, 비지니스 상에서 거래 횟수를 노출시키기 때문에 거래 ID 따로 보유

    private LocalDateTime transactedAt;
}
