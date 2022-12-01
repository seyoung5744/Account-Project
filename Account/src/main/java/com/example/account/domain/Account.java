package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity // 테이블과의 매핑 https://ttl-blog.tistory.com/112
// @Entity가 붙은 클래스는 JPA가 관리

// createdAt, updatedAt 자동 생성을 위한 어노테이션
// 또한 AuditingEntityListener을 사용하기 위해서 application 전체 설정에 추가가 필요함.
@EntityListeners(AuditingEntityListener.class)
public class Account extends BaseEntity {
    @ManyToOne // 계좌 테이블에서는 User를 n:1로 갖기 때문에...
    private AccountUser accountUser; // user가 아닌 accountUser라고 명시한 이유 : 만약 User 테이블이 존재한다면 충돌 발생 우려가 있음.
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    // Enum은 본래 0,1...로 되어 있지만 Enum에 지정한 String으로 DB 테이블에 추가하기 위해서
    private AccountStatus accountStatus;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    // 중요한 데이터를 변경하는 로직은 객체 안에서 직접 수행할 수 있도록 하는게 좋음.
    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }

        balance += amount;
    }
}
