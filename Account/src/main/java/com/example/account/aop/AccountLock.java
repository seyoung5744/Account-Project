package com.example.account.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited //상속 가능한 구조로 사용
public @interface AccountLock {
    long tryLockTime() default 5000L; // 가져온 Annotation에서 지정해준 해당 시간동안 기다려주겠다.
}
