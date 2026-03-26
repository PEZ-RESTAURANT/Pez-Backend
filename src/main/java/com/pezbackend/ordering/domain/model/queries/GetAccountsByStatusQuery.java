package com.pezbackend.ordering.domain.model.queries;

import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;

public record GetAccountsByStatusQuery(AccountStatus status) {
}