package com.youlai.boot.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank-transfer")
@Data
public class BankTransferProperties {
    private String bankName;
    private String accountName;
    private String accountNumber;
    private String iban;
    private String swiftCode;
    private String bankAddress;
    private String referenceFormat;
}
