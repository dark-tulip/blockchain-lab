package ru.amvera;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class Loan {

    @Property()
    private final String id;

    @Property()
    private final String accountId;

    @Property()
    private final int principal;

    @Property()
    private int outstanding;

    @Property()
    private String status; // ACTIVE / CLOSED

    public Loan(@JsonProperty("id") String id,
                @JsonProperty("accountId") String accountId,
                @JsonProperty("principal") int principal,
                @JsonProperty("outstanding") int outstanding,
                @JsonProperty("status") String status) {
        this.id = id;
        this.accountId = accountId;
        this.principal = principal;
        this.outstanding = outstanding;
        this.status = status;
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public int getPrincipal() { return principal; }
    public int getOutstanding() { return outstanding; }
    public String getStatus() { return status; }

    public void repay(int amount) {
        if (status.equals("CLOSED")) {
            throw new IllegalStateException("Loan already closed");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        outstanding -= amount;
        if (outstanding <= 0) {
            outstanding = 0;
            status = "CLOSED";
        }
    }
}
