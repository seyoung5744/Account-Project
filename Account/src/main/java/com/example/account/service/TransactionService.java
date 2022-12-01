package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /**
     * @Transactional 어노테이션의 이해
     * https://kafcamus.tistory.com/30
     */
    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(accountUser, account, amount);

//        // 현재 계좌에 있는 잔액
//        Long accountBalance = account.getBalance();
//
//        // 거래 후 잔액
//        // 아래처럼 계산을 하면 거래 중 위험성이 있을 수도 있기 때문에 Entity 안에서 해결할 수도 있음.
//        account.setBalance(accountBalance - amount);
        account.useBalance(amount);

        // Transaction을 TransactionDto로 변환
        // 신규 Transaction 저장

        // account.useBalance(amount) 은 Account talbe의 해당 계좌의 잔액을 변경하고
        // 아래에서 신규 Transaction 정보가 저장(insert)되는데...
        // 이 둘은 @Transactional로 묶여있기 때문에 동시에 일어나거나 동시에 일어나지 않거나 함.
        // 만약 set에서 10000원 - 5000원하는 계산을 했을 때, 바로 update 되는것은 아니라
        // insert 부분에서 작업을 진행하다가 RuntimeExceprion이 발생하게 된다면 당연히 신규 Transaction은 저장이 안되고
        // update 부분도 진행이 안되고 roll-back 하게 된다.
        return TransactionDto.fromEntity(
                saveAndGetTransaction(USE, S, account, amount)
        );
    }

    /**
     * 정책 : 사용자 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우,
     * 계좌가 이미 해지 상태인 경우, 거래금액이 잔액보다 큰 경우,
     * 거래금액이 너무 작거나 큰 경우 실패 응답 @Valid에서 해결
     */
    private void validateUseBalance(AccountUser accountUser, Account account, Long amount) {
        // 사용자 아이디와 계좌 소유주가 다른 경우
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }

        // 계좌가 이미 해지 상태인 경우
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 거래금액이 잔액보다 큰 경우
        if (amount > account.getBalance()) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(USE, F, account, amount);
    }

    private Transaction saveAndGetTransaction(
            TransactionType transactionType,
            TransactionResultType transactionResultType,
            Account account,
            Long amount) {
        return transactionRepository.save(
                // Transaction Entity에 있는 구조 그대로
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Transactional
    public TransactionDto cancelBalance(
            String transactionId,
            String accountNumber,
            Long amount
    ) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(
                saveAndGetTransaction(CANCEL, S, account, amount)
        );
    }

    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }

        if (!Objects.equals(transaction.getAmount(), amount)) {
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }

        // isBefore...LocalDateTime에서 제공
        // 1년이 넘은 거래는 사용 취소 불가능
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) {
            throw new AccountException(ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
        }
    }

    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(CANCEL, F, account, amount);
    }

    public TransactionDto queryTransaction(String transactionId) {
        return TransactionDto.fromEntity(
                transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
    }
}
