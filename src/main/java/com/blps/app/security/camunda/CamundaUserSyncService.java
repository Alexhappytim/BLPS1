package com.blps.app.security.camunda;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.repository.AppUserRepository;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CamundaUserSyncService {

    private static final Logger log = LoggerFactory.getLogger(CamundaUserSyncService.class);

    @Bean
    CommandLineRunner syncCamundaUsers(IdentityService identityService,
                                      AppUserRepository appUserRepository,
                                      FilterService filterService,
                                      TaskService taskService,
                                      AuthorizationService authorizationService) {
        return args -> {
            ensureGroup(identityService, "ROLE_ADMIN", "Administrators");
            ensureGroup(identityService, "ROLE_MENTOR", "Curators and Mentors");
            ensureGroup(identityService, "ROLE_USER", "Students");

            // Grant application access to tasklist for all roles
            grantAppAccess(authorizationService, "ROLE_ADMIN", "tasklist");
            grantAppAccess(authorizationService, "ROLE_MENTOR", "tasklist");
            grantAppAccess(authorizationService, "ROLE_USER", "tasklist");
            grantAppAccess(authorizationService, "ROLE_ADMIN", "cockpit");
            grantAppAccess(authorizationService, "ROLE_ADMIN", "admin");

            // Known default passwords
            Map<String, String> defaultPasswords = Map.of(
                    "admin", "admin12345",
                    "admin@blps.local", "admin12345",
                    "mentor@blps.local", "mentor12345",
                    "student@blps.local", "student12345"
            );

            // Sync admin system user
            syncUser(identityService, "admin", "admin12345", AppUserRole.ADMIN);

            List<AppUser> users = appUserRepository.findAll();
            for (AppUser user : users) {
                String pwd = defaultPasswords.getOrDefault(user.getLogin(), "password");
                syncUser(identityService, user.getLogin(), pwd, user.getRole());
            }

            // Ensure a default filter exists so tasks are immediately visible in Camunda Tasklist
            try {
                Filter filter = filterService.createFilterQuery().filterName("Все задачи").singleResult();
                if (filter == null) {
                    Map<String, Object> filterProperties = new HashMap<>();
                    filterProperties.put("description", "Все активные задачи процессов");
                    filterProperties.put("priority", 10);
                    filterProperties.put("refresh", true);

                    filter = filterService.newTaskFilter("Все задачи")
                            .setQuery(taskService.createTaskQuery())
                            .setProperties(filterProperties);
                    filterService.saveFilter(filter);
                    log.info("Created default Camunda Tasklist filter: Все задачи");
                }

                // Ensure filter read permission
                if (filter != null) {
                    grantFilterAccess(authorizationService, "ROLE_ADMIN", filter.getId());
                    grantFilterAccess(authorizationService, "ROLE_MENTOR", filter.getId());
                    grantFilterAccess(authorizationService, "ROLE_USER", filter.getId());
                }
            } catch (Exception e) {
                log.warn("Failed to create default Camunda filter: {}", e.getMessage());
            }
        };
    }

    public static void syncUser(IdentityService identityService, String login, AppUserRole role) {
        syncUser(identityService, login, null, role);
    }

    public static void syncUser(IdentityService identityService, String login, String rawPassword, AppUserRole role) {
        if (identityService.isReadOnly()) {
            return;
        }

        User camundaUser = identityService.createUserQuery().userId(login).singleResult();
        if (camundaUser == null) {
            camundaUser = identityService.newUser(login);
            camundaUser.setFirstName(login);
            camundaUser.setLastName(role != null ? role.name() : "USER");
            camundaUser.setEmail(login.contains("@") ? login : login + "@blps.local");
            camundaUser.setPassword(rawPassword != null ? rawPassword : "password");
            identityService.saveUser(camundaUser);
            log.info("Created Camunda user: {}", login);
        } else if (rawPassword != null && !rawPassword.isBlank()) {
            camundaUser.setPassword(rawPassword);
            identityService.saveUser(camundaUser);
        }

        if (role != null) {
            String groupId = "ROLE_" + role.name();
            Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
            if (group != null) {
                boolean alreadyMember = identityService.createUserQuery()
                        .userId(login)
                        .memberOfGroup(groupId)
                        .count() > 0;
                if (!alreadyMember) {
                    identityService.createMembership(login, groupId);
                    log.info("Added Camunda user {} to group {}", login, groupId);
                }
            }
        }
    }

    private void ensureGroup(IdentityService identityService, String groupId, String groupName) {
        if (identityService.isReadOnly()) {
            return;
        }
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (group == null) {
            group = identityService.newGroup(groupId);
            group.setName(groupName);
            group.setType("WORKFLOW");
            identityService.saveGroup(group);
            log.info("Created Camunda group: {}", groupId);
        }
    }

    private void grantAppAccess(AuthorizationService authService, String groupId, String appName) {
        try {
            long count = authService.createAuthorizationQuery()
                    .groupIdIn(groupId)
                    .resourceType(Resources.APPLICATION)
                    .resourceId(appName)
                    .count();
            if (count == 0) {
                Authorization auth = authService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                auth.setGroupId(groupId);
                auth.setResource(Resources.APPLICATION);
                auth.setResourceId(appName);
                auth.addPermission(Permissions.ACCESS);
                authService.saveAuthorization(auth);
                log.info("Granted {} access to app {}", groupId, appName);
            }
        } catch (Exception e) {
            log.debug("Could not set app authorization: {}", e.getMessage());
        }
    }

    private void grantFilterAccess(AuthorizationService authService, String groupId, String filterId) {
        try {
            long count = authService.createAuthorizationQuery()
                    .groupIdIn(groupId)
                    .resourceType(Resources.FILTER)
                    .resourceId(filterId)
                    .count();
            if (count == 0) {
                Authorization auth = authService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                auth.setGroupId(groupId);
                auth.setResource(Resources.FILTER);
                auth.setResourceId(filterId);
                auth.addPermission(Permissions.READ);
                authService.saveAuthorization(auth);
                log.info("Granted {} access to filter {}", groupId, filterId);
            }
        } catch (Exception e) {
            log.debug("Could not set filter authorization: {}", e.getMessage());
        }
    }
}
