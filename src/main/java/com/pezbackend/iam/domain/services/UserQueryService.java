package io.github.rafaviv.panther.pantherbackend.iam.domain.services;

import io.github.rafaviv.panther.pantherbackend.iam.domain.model.aggregates.User;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.queries.GetAllUsersQuery;
import io.github.rafaviv.panther.pantherbackend.iam.domain.model.queries.GetUserByEmailQuery;


import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for query-based (read-only) operations on the User aggregate.
 * Implementations are typically found in the Application layer.
 */
public interface UserQueryService {
    /**
     * Handles the query to find a user by email.
     * @param query The query containing the email.
     * @return An Optional containing the User aggregate if found.
     */
    Optional<User> handle(GetUserByEmailQuery query);

    /**
     * Handles the query to retrieve all users.
     * @param query The query (empty, used for consistency).
     * @return A list of all User aggregates.
     */
    List<User> handle(GetAllUsersQuery query);
}
