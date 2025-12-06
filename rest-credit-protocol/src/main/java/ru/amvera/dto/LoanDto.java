package ru.amvera.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private String id;
    private String accountId;
    private int principal;
    private int outstanding;
    private String status;
}
