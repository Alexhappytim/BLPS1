package com.blps.app;

import com.blps.app.auth.AuthManagementService;
import com.blps.app.auth.dto.RegistrationResponse;
import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.repository.AppUserRepository;
import com.blps.app.domain.repository.EmailVerificationTokenRepository;
import com.blps.app.infrastructure.crm.CrmClient;
import com.blps.app.infrastructure.messaging.mail.MailDispatchService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthManagementServiceTest {

    private AppUserRepository appUserRepository;
    private EmailVerificationTokenRepository tokenRepository;
    private PasswordEncoder passwordEncoder;
    private MailDispatchService mailDispatchService;
    private CrmClient crmClient;
    private IdentityService identityService;
    private AuthManagementService authManagementService;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        tokenRepository = mock(EmailVerificationTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        mailDispatchService = mock(MailDispatchService.class);
        crmClient = mock(CrmClient.class);
        identityService = mock(IdentityService.class);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pwd");

        // IdentityService mock setup
        UserQuery userQuery = mock(UserQuery.class);
        when(identityService.createUserQuery()).thenReturn(userQuery);
        when(userQuery.userId(anyString())).thenReturn(userQuery);
        when(userQuery.singleResult()).thenReturn(null);
        when(userQuery.memberOfGroup(anyString())).thenReturn(userQuery);
        when(userQuery.count()).thenReturn(0L);

        User newUser = mock(User.class);
        when(identityService.newUser(anyString())).thenReturn(newUser);

        GroupQuery groupQuery = mock(GroupQuery.class);
        when(identityService.createGroupQuery()).thenReturn(groupQuery);
        when(groupQuery.groupId(anyString())).thenReturn(groupQuery);
        Group group = mock(Group.class);
        when(groupQuery.singleResult()).thenReturn(group);

        authManagementService = new AuthManagementService(
                appUserRepository,
                tokenRepository,
                passwordEncoder,
                mailDispatchService,
                crmClient,
                identityService,
                24L
        );
    }

    @Test
    void testRegisterUser_SyncsToCamunda() {
        when(appUserRepository.existsByLogin("student@test.local")).thenReturn(false);

        RegistrationResponse response = authManagementService.registerUser("student@test.local", "plain_password");

        Assertions.assertEquals("student@test.local", response.email());
        Assertions.assertEquals(AppUserRole.USER, response.role());
        verify(appUserRepository).save(any(AppUser.class));
        verify(identityService).newUser("student@test.local");
        verify(identityService).saveUser(any(User.class));
        verify(identityService).createMembership("student@test.local", "ROLE_USER");
    }

    @Test
    void testRegisterAdmin_SyncsToCamunda() {
        when(appUserRepository.existsByLogin("admin2@test.local")).thenReturn(false);

        RegistrationResponse response = authManagementService.registerByAdmin("admin2@test.local", "admin_pwd", AppUserRole.ADMIN);

        Assertions.assertEquals("admin2@test.local", response.email());
        Assertions.assertEquals(AppUserRole.ADMIN, response.role());
        verify(appUserRepository).save(any(AppUser.class));
        verify(identityService).newUser("admin2@test.local");
        verify(identityService).saveUser(any(User.class));
        verify(identityService).createMembership("admin2@test.local", "ROLE_ADMIN");
    }

    @Test
    void testRegisterCurator_SyncsToCamunda() {
        when(appUserRepository.existsByLogin("curator@test.local")).thenReturn(false);

        RegistrationResponse response = authManagementService.registerByAdmin("curator@test.local", "curator_pwd", AppUserRole.MENTOR);

        Assertions.assertEquals("curator@test.local", response.email());
        Assertions.assertEquals(AppUserRole.MENTOR, response.role());
        verify(appUserRepository).save(any(AppUser.class));
        verify(identityService).newUser("curator@test.local");
        verify(identityService).saveUser(any(User.class));
        verify(identityService).createMembership("curator@test.local", "ROLE_MENTOR");
    }
}
