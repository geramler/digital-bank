package com.digitalbank.notification.messaging;

import com.digitalbank.events.AccountCreatedEvent;
import com.digitalbank.events.CustomerCreatedEvent;
import com.digitalbank.events.TransactionCompletedEvent;
import com.digitalbank.events.TransferEvent;
import com.digitalbank.notification.model.Notification;
import com.digitalbank.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final NotificationRepository notificationRepository;

    public EventConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "customer.created", groupId = "notification-service")
    public void onCustomerCreated(CustomerCreatedEvent event) {
        log.info("Notification: customer.created eventId={} customerId={} email={}", event.eventId(), event.customerId(), event.email());
        notificationRepository.save(new Notification(
                event.email(),
                "Welcome to Digital Bank, " + event.name() + "! Your account has been created.",
                "EMAIL",
                "CUSTOMER_CREATED"
        ));
    }

    @KafkaListener(topics = "account.created", groupId = "notification-service")
    public void onAccountCreated(AccountCreatedEvent event) {
        log.info("Notification: account.created eventId={} accountId={} customerId={} type={} balance={}",
                event.eventId(), event.accountId(), event.customerId(), event.accountType(), event.initialBalance());
        notificationRepository.save(new Notification(
                "customer-" + event.customerId() + "@example.com",
                "Your new " + event.accountType() + " account #" + event.accountId()
                        + " has been created with balance $" + event.initialBalance() + ".",
                "EMAIL",
                "ACCOUNT_CREATED"
        ));
    }

    @KafkaListener(topics = "transaction.completed", groupId = "notification-service")
    public void onTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Notification: transaction.completed eventId={} txId={} type={} amount={}",
                event.eventId(), event.transactionId(), event.type(), event.amount());
        notificationRepository.save(new Notification(
                "account-" + event.accountId() + "@example.com",
                "Transaction #" + event.transactionId() + " of type " + event.type()
                        + " for $" + event.amount() + " completed.",
                "EMAIL",
                "TRANSACTION_COMPLETED"
        ));
    }

    @KafkaListener(topics = "transfer.events", groupId = "notification-service")
    public void onTransferEvent(TransferEvent event) {
        String msg;
        String channel = "EMAIL";

        switch (event.type()) {
            case INITIATED -> msg = "Transfer #" + event.transferId() + " initiated from account "
                    + event.fromAccountId() + " to " + event.toAccountId() + " for $" + event.amount() + ".";
            case COMPLETED -> msg = "Transfer #" + event.transferId() + " completed successfully! $"
                    + event.amount() + " transferred from account " + event.fromAccountId()
                    + " to " + event.toAccountId() + ".";
            case FAILED -> msg = "Transfer #" + event.transferId() + " failed. Please try again.";
            default -> {
                return;
            }
        }

        log.info("Notification: transfer event type={} transferId={}", event.type(), event.transferId());
        notificationRepository.save(new Notification(
                "account-" + event.fromAccountId() + "@example.com",
                msg,
                channel,
                "TRANSFER_" + event.type().name()
        ));
    }
}