package com.blps.app.security.camunda;

import com.blps.app.domain.model.AppUser;
import com.blps.app.domain.model.AppUserRole;
import com.blps.app.domain.repository.AppUserRepository;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CamundaUserSyncService {

    private static final Logger log = LoggerFactory.getLogger(CamundaUserSyncService.class);

    @Bean
    CommandLineRunner syncCamundaUsers(IdentityService identityService, AppUserRepository appUserRepository) {
        return args -> {
            ensureGroup(identityService, "ROLE_ADMIN", "Administrators");
            ensureGroup(identityService, "ROLE_MENTOR", "Curators and Mentors");
            ensureGroup(identityService, "ROLE_USER", "Students");

            List<AppUser> users = appUserRepository.findAll();
            for (AppUser user : users) {
                syncUser(identityService, user.getLogin(), user.getRole());
            }
        };
    }

    public static void syncUser(IdentityService identityService, String login, AppUserRole role) {
        if (identityService.isReadOnly()) {
            return;
        }

        User camundaUser = identityService.createUserQuery().userId(login).singleResult();
        if (camundaUser == null) {
            camundaUser = identityService.newUser(login);
            camundaUser.setFirstName(login);
            camundaUser.setLastName(role.name());
            camundaUser.setEmail(login.contains("@") ? login : login + "@blps.local");
            camundaUser.setPassword("password"); // Default or user password for Tasklist login
            identityService.saveUser(camundaUser);
            log.info("Created Camunda user: {}", login);
        }

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
}
