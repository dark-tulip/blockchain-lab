package ru.amvera.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanIssueRequest {
    private String loanId;
    private String accountId;
    private int principal;
}
