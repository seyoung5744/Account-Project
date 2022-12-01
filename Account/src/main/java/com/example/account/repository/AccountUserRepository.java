package com.example.account.repository;

import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 조회할 테이블 Entity, PK type
@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
}
