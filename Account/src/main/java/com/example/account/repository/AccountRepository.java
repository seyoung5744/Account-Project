package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// @Entity를 DB에 저장하기 위해서는 JPA에서 제공해주는 Repository가 필요
// https://dbjh.tistory.com/77


// Account라는 table에 접속하기 위한 interface
// 2가지 Type
// 1. 해당 Repository가 활용하게될 Entity
// 2. 해당 Entity의 primary key의 타입
@Repository // Bean 등록을 위한 @Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // method 명칭을 규칙에 맞게 쓰면 자동으로 Query를 생성해준다.
    // 또한 이때 값이 없을 수도 있기 때문에 Optional 방식으로 가져오겠다~
    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(AccountUser accountUser);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
