package com.example.account.service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber) {
        // 1. RedissonClient에서 "ACLK:" + accountNumber이란 이름으로 lock key 사용
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try {
            // 2. 가져온 lock으로 spinLock 시도. 1초동안 기다리면서 lock 획득 시도
            // lock 획득 성공 시 5초 후에 자동으로 lock을 Lease
            // lock을 5초간 갖고 있기 때문에 다른 곳에서 획득하려고 한다면 5초동안은 실패하게 됨.
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);

            if (!isLock) { // 1초 동안의 lock 획득에 실패하면....
                log.error("======Lock acquisition failed=====");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock failed", e);
        }
    }

    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber : {}", accountNumber);
        // lock을 가져온 뒤 해제
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }

    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
