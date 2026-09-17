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
            ensureGroup(identityService, "camunda-admin", "Camunda Administrators");

            // 1. Grant application access:
            // Tasklist is accessible to students, mentors and admins
            grantAppAccess(authorizationService, "ROLE_ADMIN", "tasklist");
            grantAppAccess(authorizationService, "ROLE_MENTOR", "tasklist");
            grantAppAccess(authorizationService, "ROLE_USER", "tasklist");
            // Cockpit and Admin are accessible ONLY to ROLE_ADMIN
            grantAppAccess(authorizationService, "ROLE_ADMIN", "cockpit");
            grantAppAccess(authorizationService, "ROLE_ADMIN", "admin");

            // 2. Grant resource permissions:
            // Admin has ALL access
            grantResourcePermissions(authorizationService, "ROLE_ADMIN", Resources.TASK, Permissions.ALL);
            grantResourcePermissions(authorizationService, "ROLE_ADMIN", Resources.PROCESS_DEFINITION, Permissions.ALL);
            grantResourcePermissions(authorizationService, "ROLE_ADMIN", Resources.PROCESS_INSTANCE, Permissions.ALL);
            grantResourcePermissions(authorizationService, "ROLE_ADMIN", Resources.DEPLOYMENT, Permissions.ALL);
            grantResourcePermissions(authorizationService, "ROLE_ADMIN", Resources.BATCH, Permissions.ALL);

            // Mentors can read & update tasks (for review) and read process definitions/instances
            grantResourcePermissions(authorizationService, "ROLE_MENTOR", Resources.TASK, Permissions.READ, Permissions.UPDATE);
            grantResourcePermissions(authorizationService, "ROLE_MENTOR", Resources.PROCESS_DEFINITION, Permissions.READ);
            grantResourcePermissions(authorizationService, "ROLE_MENTOR", Resources.PROCESS_INSTANCE, Permissions.READ);

            // Students can read & update their tasks and start process instances
            grantResourcePermissions(authorizationService, "ROLE_USER", Resources.TASK, Permissions.READ, Permissions.UPDATE);
            grantResourcePermissions(authorizationService, "ROLE_USER", Resources.PROCESS_DEFINITION, Permissions.READ, Permissions.CREATE_INSTANCE);
            grantResourcePermissions(authorizationService, "ROLE_USER", Resources.PROCESS_INSTANCE, Permissions.CREATE, Permissions.READ);

            // 3. Known default passwords & Sync Users
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

            // 4. Create separate Role-Based Tasklist Filters
            try {
                // Filter 1: "Мои задачи" — assigned to the current user (${currentUser()})
                ensureFilter(filterService, taskService, authorizationService,
                        "Мои задачи",
                        "Задачи, назначенные персонально на текущего пользователя",
                        10,
                        taskService.createTaskQuery().taskAssigneeExpression("${currentUser()}"),
                        List.of("ROLE_USER", "ROLE_MENTOR", "ROLE_ADMIN")
                );

                // Filter 2: "На проверку (Менторы)" — review tasks for mentors
                ensureFilter(filterService, taskService, authorizationService,
                        "На проверку (Менторы)",
                        "Задачи студентов, ожидающие проверки ментором",
                        20,
                        taskService.createTaskQuery().taskCandidateGroup("ROLE_MENTOR"),
                        List.of("ROLE_MENTOR", "ROLE_ADMIN")
                );

                // Filter 3: "Все задачи (Администратор)" — full visibility for admins only
                ensureFilter(filterService, taskService, authorizationService,
                        "Все задачи (Администратор)",
                        "Все активные задачи процессов системы",
                        30,
                        taskService.createTaskQuery(),
                        List.of("ROLE_ADMIN")
                );
            } catch (Exception e) {
                log.warn("Failed to configure Camunda filters: {}", e.getMessage());
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

            if (role == AppUserRole.ADMIN) {
                boolean adminMember = identityService.createUserQuery()
                        .userId(login)
                        .memberOfGroup("camunda-admin")
                        .count() > 0;
                if (!adminMember) {
                    identityService.createMembership(login, "camunda-admin");
                    log.info("Added Camunda admin {} to camunda-admin group", login);
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

    private void grantResourcePermissions(AuthorizationService authService, String groupId, Resources resource, Permissions... permissions) {
        try {
            long count = authService.createAuthorizationQuery()
                    .groupIdIn(groupId)
                    .resourceType(resource)
                    .resourceId("*")
                    .count();
            if (count == 0) {
                Authorization auth = authService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
                auth.setGroupId(groupId);
                auth.setResource(resource);
                auth.setResourceId("*");
                for (Permissions p : permissions) {
                    auth.addPermission(p);
                }
                authService.saveAuthorization(auth);
                log.info("Granted {} permissions on resource {}", groupId, resource.resourceName());
            }
        } catch (Exception e) {
            log.debug("Could not set resource authorization for {}: {}", groupId, e.getMessage());
        }
    }

    private void ensureFilter(FilterService filterService,
                              TaskService taskService,
                              AuthorizationService authorizationService,
                              String filterName,
                              String description,
                              int priority,
                              org.camunda.bpm.engine.task.TaskQuery query,
                              List<String> groupIds) {
        Filter filter = filterService.createFilterQuery().filterName(filterName).singleResult();
        if (filter == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("description", description);
            properties.put("priority", priority);
            properties.put("refresh", true);

            filter = filterService.newTaskFilter(filterName)
                    .setQuery(query)
                    .setProperties(properties);
            filterService.saveFilter(filter);
            log.info("Created Tasklist filter: {}", filterName);
        }

        if (filter != null) {
            for (String groupId : groupIds) {
                grantFilterAccess(authorizationService, groupId, filter.getId());
            }
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
