package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {
    // Spring 레이어드 아키텍처 https://stdbc.tistory.com/20
    // 외부에선 Controller로만 접속
    // Controller는 Service로만 접속
    // Service는 Repository로만 접속
    private final AccountService accountService;


    @PostMapping("/account")// Post방식을 사용하기 때문에 RequestBody를 사용하는 방식으로 진행
    public CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {

        // Service와 통신할 때 사용한 AccountDto를 Response로 바꿔주기
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()
                ));
    }

    @DeleteMapping("/account")
    public DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ) {

        // Service와 통신할 때 사용한 AccountDto를 Response로 바꿔주기
        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()
                ));
    }

    @GetMapping("/account")
    public List<AccountInfo> getAccountByUserId(
            @RequestParam("user_id") Long userId
    ) {
        return accountService.getAccountByUserId(userId)
                .stream().map(accountDto -> AccountInfo.builder()
                        .accountNumber(accountDto.getAccountNumber())
                        .balance(accountDto.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/account/{id}")
    public Account getAccount(
            @PathVariable Long id) {
        return accountService.getAccount(id);
    }


}
