package com.izenshy.gessainvoice.modules.cashregister.model;

import com.izenshy.gessainvoice.modules.invoice.model.InvoiceModel;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_transactions")
@NamedQuery(name = "CashTransactionModel.findAll", query = "SELECT ct FROM CashTransactionModel ct")
@Data
public class CashTransactionModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "transaction_uuid", columnDefinition = "UUID DEFAULT uuid_generate_v4()", unique = true, updatable = false)
    private UUID transactionUuid;

    @PrePersist
    public void prePersist() {
        if (transactionUuid == null) {
            transactionUuid = UUID.randomUUID();
        }
    }

    @ManyToOne
    @JoinColumn(name = "cash_register_id")
    private CashRegisterModel cashRegisterId;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoiceModel invoiceId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "amount_cash", precision = 12, scale = 2)
    private BigDecimal amountCash;

    @Column(name = "amount_transfer", precision = 12, scale = 2)
    private BigDecimal amountTransfer;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "recipient")
    private String recipient;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel userId;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;
}