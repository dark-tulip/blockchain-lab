package ru.amvera.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.amvera.services.CreditFabricService;
import ru.amvera.requests.DepositRequest;
import ru.amvera.requests.RepayRequest;
import ru.amvera.requests.TransferRequest;
import ru.amvera.dto.AccountDto;
import ru.amvera.dto.LoanDto;


@Slf4j
@RestController
@RequestMapping("/api")
public class CreditController {

    private final CreditFabricService fabricService;

    public CreditController(CreditFabricService fabricService) {
        this.fabricService = fabricService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody AccountDto req) {
        try {
            String result = fabricService.createAccount(req.getId(), req.getOwner());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<?> getAccount(@PathVariable String id) {
        try {
            String result = fabricService.readAccount(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // по желанию — остальные:

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable String id, @RequestBody DepositRequest req) {
        try {
            String result = fabricService.deposit(id, req.getAmount());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/loans")
    public ResponseEntity<?> issueLoan(@RequestBody LoanDto req) {
        try {
            log.debug("DEBUG /loans: id=" + req.getId()
                + ", accountId=" + req.getAccountId()
                + ", principal=" + req.getPrincipal());

            if (req.getId() == null || req.getId().isBlank()) {
                return ResponseEntity.badRequest().body("loan id (id) is required");
            }
            if (req.getAccountId() == null || req.getAccountId().isBlank()) {
                return ResponseEntity.badRequest().body("accountId is required");
            }

            String result = fabricService.issueLoan(
                req.getId(),
                req.getAccountId(),
                req.getPrincipal()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/loans/legacy")
    public ResponseEntity<?> issueLoanLegacy(@RequestBody LoanDto req) {
        // здесь ты имитируешь старую систему:
        // - проверка в "внешнем сервисе" (можешь сделать Thread.sleep(150-300)
        // - запись в БД или просто возврат JSON

        String result = "{ \"status\": \"OK\", \"id\": \"" + req.getId() + "\" }";
        return ResponseEntity.ok(result);
    }


    @GetMapping("/loans/{id}")
    public ResponseEntity<?> getLoan(@PathVariable String id) {
        try {
            String result = fabricService.readLoan(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/loans/{id}/repay")
    public ResponseEntity<?> repay(@PathVariable String id, @RequestBody RepayRequest req) {
        try {
            String result = fabricService.repayLoan(id, req.getAmount());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfers")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest req) {
        try {
            String result = fabricService.transfer(req.getFromId(), req.getToId(), req.getAmount());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
