package com.pezbackend.iam.application.internal.outboundservices.tokens;



/**
 * TokenService interface
 * This interface is used to generate and validate tokens
 */
public interface TokenService {

    /**
     * Generate a token for a given username and tenant ID
     * @param userId the user ID
     * @param userRole the user role
     * @return String the token
     */
    String generateToken(Long userId, String userRole);

    /**
     * Generate a token for a given user, role, and restaurant ID
     * @param userId the user ID
     * @param userRole the user role
     * @param restaurantId the restaurant ID
     * @return String the token
     */
    String generateToken(Long userId, String userRole, Long restaurantId);

    /**
     * Extract the restaurant ID from a token
     * @param token the token
     * @return Long the restaurant ID
     */
    Long getRestaurantIdFromToken(String token);

    /**
     * Extract the username from a token
     * @param token the token
     * @return Long the userId
     */
    Long getUserIdFromToken(String token);

    /**
     * Extract the workshop ID from a token
     * @param token the token
     * @return Long the workshop ID
     */
    Long getWorkshopIdFromToken(String token);

    /**
     * Validate a token
     * @param token the token
     * @return boolean true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}