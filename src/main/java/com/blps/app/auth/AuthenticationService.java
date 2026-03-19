package com.blps.app.auth;

public interface AuthenticationService {

    AuthenticatedUser authenticateByLogin(String login);
}
