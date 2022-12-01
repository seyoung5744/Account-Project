package com.example.account.service;

import com.example.account.aop.AccountLockIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;


    // 어떤 경우에 해당 Aop를 적용할 지
    // package 명로 다 적어줘야 함.
    @Around("@annotation(com.example.account.aop.AccountLock) && args(request)")
    // @AccountLock을 붙인 부분의 request를 args(request)로 가져올 수 있음.
    // 단, 가져오는 부분에서 타입이 다르기 때문에 타입 공통화 작업을 위해 인터페이스 이용
    public Object aroundMethod(
            ProceedingJoinPoint pjp,
            AccountLockIdInterface request
    ) throws Throwable{
        // lock 취득
        lockService.lock(request.getAccountNumber());
        try{
            // 진행하고 있던 JoinPoint를 가져와서 진행
            // @Around : before, after 모든 부분을 둘러싸면서 우리가 원하는 동작을 넣어줄 수 있음.
            return pjp.proceed();
        }finally {
            // lock이 성공하든 실패하든 최종적으로는 lock 해제
            lockService.unlock(request.getAccountNumber());
        }
    }
}
