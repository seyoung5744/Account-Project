package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    // 우리가 테스트할 부분은 AccountService. 하지만 AccountService에는 Repository가 의존되어 있어서 생성이 필요.
    // 이때 Mockito를 이용하여 '가짜'를 생성해줌.
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks// 위에서 만든 '가짜' AccountRepository를 AccountService에 주입
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("Pobi").build();
        accountUser.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
//        assertEquals("1000000015", accountDto.getAccountNumber());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }


    @Test
    @DisplayName("아무 계좌도 없는 상활일 때")
    void createFirstAccount() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("Pobi").build();
        accountUser.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty()); // 아무 계좌도 없는 상활일 때

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000015").build());

        // save에서 저장되는 실제 계좌는 captor 안으로 들어감.
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌 생성 시 유저가 없는 경우 - 계좌 생성 실패")
    void createAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

    // accountUser를 AccountRepository를 이용해 조회하여 몇 개의 계좌를 갖고 있는지 확인
    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("Pobi").build();
        accountUser.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());

    }


    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("Pobi").build();
        accountUser.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));


        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);


        //when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("계좌 해지 시 유저가 없는 경우 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        AccountUser accountUser = AccountUser.builder()
                .name("Pobi").build();
        accountUser.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주가 다를 때")
    void deleteAccountFailed_userUnMatch() {
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);

        AccountUser harry = AccountUser.builder()
                .name("Harry").build();
        harry.setId(13L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(harry) // 계좌 찾았을 때 소유주는 harry
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi) // 계좌 찾았을 때 소유주는 pobi
                        .balance(100L) // 근데 잔액이 남아있으면
                        .accountNumber("1000000012").build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
    }

    @Test
    @DisplayName("이미 해지된 경우")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(pobi) // 소유주 맞고
                        .balance(0L) // 남은 잔액도 없지만
                        .accountStatus(AccountStatus.UNREGISTERED) // 이미 해지된 상태라면
                        .accountNumber("1000000012").build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }


    @Test
    void successGetAccountsByUserId() {
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi").build();
        pobi.setId(12L);

        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("1111111111")
                        .balance(1000L).build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("2222222222")
                        .balance(2000L).build(),
                Account.builder()
                        .accountUser(pobi)
                        .accountNumber("3333333333")
                        .balance(3000L).build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        //when
        List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);// 1L처럼 들어가는 값은 아무 값이나 가능

        //then
        assertEquals(3, accountDtos.size());
        assertEquals("1111111111",accountDtos.get(0).getAccountNumber());
        assertEquals(1000L,accountDtos.get(0).getBalance());
        assertEquals("2222222222",accountDtos.get(1).getAccountNumber());
        assertEquals(2000L,accountDtos.get(1).getBalance());
        assertEquals("3333333333",accountDtos.get(2).getAccountNumber());
        assertEquals(3000L,accountDtos.get(2).getBalance());
    }

    @Test
    @DisplayName("사용자 아이디가 없을 때")
    void failedToGetAccounts() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }
}