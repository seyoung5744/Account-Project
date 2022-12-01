package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service // Repository를 사용하기 위한 Service
@RequiredArgsConstructor // 꼭 필요한 Args가 들어간 생성자를 만들어 주겠다.
// 조건 : Type을 final로 하여 해당 변수는 생성자가 아니면 초기화가 불가능하도록 만든다.
// final이 붙거나 @NotNull 이 붙은 필드의 생성자를 자동 생성해주는 Lombok 어노테이션
public class AccountService {
    // @Autowired : 기존에는 필드 삽입으로 만들었지만 지금은 생성자에서 값을 넣으라고 함.
    // 하지만 생성자에서 값을 할당하게 된다면...필드가 여러 개 있거나 수정이 빈번할 때 생성자 생성 및 수정이 불편하다.
    // 그래서 사용되는 것이 Lombok의 @RequiredArgsConstructor
    private final AccountRepository accountRepository;

    // 사용자 조회를 위한 AccountUserRepository
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌 번호 생성하고
     * 계좌를 저장하고, 그 정보를 넘긴다.
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) { // Account 테이블에 계좌 데이터를 저장
        // userId를 이용해서 사용자 조회
        AccountUser accountUser = getAccountUser(userId);

        validateCreateAccount(accountUser);

        // 마지막에 생성된 계좌 번호 + 1을 하여 새로운 계좌 번호 생성
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                // 계좌 번호가 있다면 문자열 -> 정수 + 1 -> 문자열
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                // 현재 계좌가 하나도 없으면 아래 값 할당.
                .orElse("1000000000");

        // Repository에 저장.
        // save 대상은 Account Entity를 build 해준 대상

        // 문제점 : Entity 클래스는 일반적인 클래스와는 다른 성격의 클래스이기 때문에
        // 해당 클래스로 layer 안에서 주고 받게되면 Entity에서 lazy loading이라든지 추가적인 query를 날리려고 하다보면
        // 트랜잭션이 없기 때문에 오류가 발생할 우려가 있다.
        // 또한 Controller에서 응답을 줄 때 데이터들이 Entity 정보 이외에도 추가적인 혹은 더 적은 정보만 응답을 줄 수도 있는 경우처럼 요구사항들이
        // 변할 수도 있다. 그래서 Controller와 Service 간 통신할 때 사용될 별도의 Dto가 요구된다.


        // 생성한 Account Entity를 Repository를 통해 저장하고
        // .save의 반환값인 Account Entity를 활용해서 fromEntity에서 저장된 Account Entity에서 필요한 정보만 뽑아오고
        // Account Dto를 만들어서 return
        // 그렇게하면 Controller에서 받아서 사용
        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build())
        );
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        // 사용자가 없는 경우
        AccountUser accountUser = getAccountUser(userId);
        // 계좌가 없는 경우
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 사용자 아이디와 계좌 소유주가 다른 경우
        // 계좌가 이미 해지 상태인 경우
        // 잔액이 있는 경우 실패 응답
        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        // 원활한 테스트를 위해 추가
        // update는 settter를 통해 자동으로 update가 되지만 테스트를 위해 불필요한 코드를 추가하는 것도 좋을 때가 있음
        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 사용자 아이디와 계좌 소유주가 다른 경우
        if (accountUser.getId() != account.getAccountUser().getId()) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }

        // 계좌가 이미 해지 상태인 경우
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 잔액이 있는 경우 실패 응답
        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }


    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        // 요청 받은 userId에 해당하는 AccountUser 가져오기
        AccountUser accountUser = getAccountUser(userId);

        // 해당 유저랑 연결된 계좌 리스트 가져오기
        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity) // 함수를 인자로 넣어주면 각 Account가 AccountDto로 변환
                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
    }
}
