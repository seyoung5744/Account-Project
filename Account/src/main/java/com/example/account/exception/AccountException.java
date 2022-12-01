package com.example.account.exception;

import com.example.account.type.ErrorCode;
import lombok.*;

// 체크 예외와 언체크 예외 https://devlog-wjdrbs96.tistory.com/351, https://www.nextree.co.kr/p3239/
// 그냥 Exception을 extends하면 체크 예외가 되기 때문에 트랜잭션 roll-back 대상이 되지 않기 때문에 불편함.
// 그래서 요즘에는 언체크 예외를 주로 사용한다.
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountException extends RuntimeException {
    // 기본적으로 RuntimeException애는 예외 정보가 적기 때문에 도움이 되는 정보가 없다.
    // 그래서 AccountException을 던진다고 해도 활용가치가 적음.
    // 해결 책으로 AccountNotFoundException..등 여러 예외 클래스를 만드는 방법도 있겠지만 클래스가 많아지면 복잡해짐.
    private ErrorCode errorCode;
    private String errorMessage;

    public AccountException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
