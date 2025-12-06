package ru.amvera;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeStub;

@Contract(
        name = "CreditContract",
        info = @Info(
                title = "Credit Protocol",
                description = "Кредитный протокол банка на Hyperledger Fabric",
                version = "1.0"
        )
)
@Default
public class CreditContract implements ContractInterface {

    private final Genson genson = new Genson();

    // ==== Вспомогательные методы ====
    private String accountKey(String accountId) {
        return "ACC_" + accountId;
    }

    private String loanKey(String loanId) {
        return "LOAN_" + loanId;
    }

    private Account getAccountOrThrow(ChaincodeStub stub, String accountId) {
        String data = stub.getStringState(accountKey(accountId));
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Account " + accountId + " does not exist");
        }
        return genson.deserialize(data, Account.class);
    }

    private Loan getLoanOrThrow(ChaincodeStub stub, String loanId) {
        String data = stub.getStringState(loanKey(loanId));
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Loan " + loanId + " does not exist");
        }
        return genson.deserialize(data, Loan.class);
    }

    // ==== Транзакции ====

    @Transaction()
    public boolean accountExists(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();
        String data = stub.getStringState(accountKey(accountId));
        return data != null && !data.isEmpty();
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Account createAccount(final Context ctx,
                                 final String accountId,
                                 final String owner) {

        ChaincodeStub stub = ctx.getStub();
        if (accountExists(ctx, accountId)) {
            throw new RuntimeException("Account already exists: " + accountId);
        }

        Account acc = new Account(accountId, owner, 0);
        String json = genson.serialize(acc);
        stub.putStringState(accountKey(accountId), json);
        return acc;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Account deposit(final Context ctx,
                           final String accountId,
                           final int amount) {

        ChaincodeStub stub = ctx.getStub();
        Account acc = getAccountOrThrow(stub, accountId);
        acc.deposit(amount);
        stub.putStringState(accountKey(accountId), genson.serialize(acc));
        return acc;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Account transfer(final Context ctx,
                            final String fromId,
                            final String toId,
                            final int amount) {

        ChaincodeStub stub = ctx.getStub();

        Account from = getAccountOrThrow(stub, fromId);
        Account to = getAccountOrThrow(stub, toId);

        from.withdraw(amount);
        to.deposit(amount);

        stub.putStringState(accountKey(fromId), genson.serialize(from));
        stub.putStringState(accountKey(toId), genson.serialize(to));

        return from; // можно вернуть что угодно, напр. оба счёта
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Loan issueLoan(final Context ctx,
                          final String loanId,
                          final String accountId,
                          final int principal) {

        ChaincodeStub stub = ctx.getStub();

        // пополняем счёт заёмщика
        Account borrower = getAccountOrThrow(stub, accountId);
        borrower.deposit(principal);
        stub.putStringState(accountKey(accountId), genson.serialize(borrower));

        // создаём кредит
        Loan loan = new Loan(loanId, accountId, principal, principal, "ACTIVE");
        stub.putStringState(loanKey(loanId), genson.serialize(loan));

        return loan;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Loan repayLoan(final Context ctx,
                          final String loanId,
                          final int amount) {

        ChaincodeStub stub = ctx.getStub();

        Loan loan = getLoanOrThrow(stub, loanId);
        Account borrower = getAccountOrThrow(stub, loan.getAccountId());

        borrower.withdraw(amount);
        loan.repay(amount);

        stub.putStringState(accountKey(borrower.getId()), genson.serialize(borrower));
        stub.putStringState(loanKey(loanId), genson.serialize(loan));

        return loan;
    }

    @Transaction()
    public Account readAccount(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();
        return getAccountOrThrow(stub, accountId);
    }

    @Transaction()
    public Loan readLoan(final Context ctx, final String loanId) {
        ChaincodeStub stub = ctx.getStub();
        return getLoanOrThrow(stub, loanId);
    }
}
