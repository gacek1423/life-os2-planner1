package com.budget.modules.finance.events;

import com.budget.infrastructure.DomainEvent;
import com.budget.model.Transaction;

public record TransactionAddedEvent(Transaction transaction) implements DomainEvent {}