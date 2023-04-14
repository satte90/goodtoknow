package com.teliacompany.tiberius.user.service;

import com.teliacompany.ldap.model.LdapGroup;
import com.teliacompany.ldap.model.LdapUser;
import com.teliacompany.ldap.service.LdapService;
import com.teliacompany.webflux.error.exception.client.BadRequestException;
import com.teliacompany.webflux.error.exception.server.InternalServerErrorException;
import com.teliacompany.tiberius.user.api.v1.Role;
import com.teliacompany.tiberius.user.api.v1.UserResponse;
import com.teliacompany.tiberius.user.cache.LdapGroupCache;
import com.teliacompany.tiberius.user.converter.v1.RetailerConverter;
import com.teliacompany.tiberius.user.converter.v1.RoleConverter;
import com.teliacompany.tiberius.user.model.RetailerEntity;
import com.teliacompany.tiberius.user.model.RoleType;
import com.teliacompany.tiberius.user.model.UserEntity;
import com.teliacompany.tiberius.user.repository.RetailerRepository;
import com.teliacompany.tiberius.user.repository.UserRepository;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RetailerRepository retailerRepository;
    private final LdapService ldapService;

    private final String rolePrefixIdm;
    private final String rolePrefixTiga;
    private final Boolean validateRoles;

    private final LdapGroupCache ldapGroupCache;

    public UserService(UserRepository userRepository,
                       RetailerRepository retailerRepository,
                       LdapService ldapService,
                       LdapGroupCache ldapGroupCache,
                       @Value("${idm.role.prefix}") String rolePrefixIdm,
                       @Value("${tiga.role.prefix}") String rolePrefixTiga,
                       @Value("${idm.role.validation}") String validateRoles) {
        this.userRepository = userRepository;
        this.retailerRepository = retailerRepository;
        this.ldapService = ldapService;

        this.ldapGroupCache = ldapGroupCache;
        this.rolePrefixIdm = rolePrefixIdm;
        this.rolePrefixTiga = StringEscapeUtils.unescapeJava(rolePrefixTiga);
        this.validateRoles = Boolean.valueOf(validateRoles);
    }

    public Mono<UserResponse> saveUser(UserEntity userEntity) {
        final Context context = new Context(userEntity.getTcad())
                .addUser(userEntity);
        return this.setFieldsIfMissingInUpdate(context)
                .flatMap(this::getRetailerForUser)
                .flatMap(this::getLdapUser)
                .switchIfEmpty(Mono.error(new BadRequestException("User with tcad " + userEntity.getTcad() + " does not exist in ldap")))
                .flatMap(this::validateRoles)
                .flatMap(this::saveUser)
                .map(this::makeUserResponse);
    }

    public Mono<UserResponse> getUser(String tcad) {
        return Mono.just(new Context(tcad))
                //.flatMap(this::getUserEntity)
                //.flatMap(this::getRetailerForUser)
                .flatMap(this::getLdapUser)
                .flatMap(this::getAuthorizedRoles)
                .map(this::makeUserResponse);
    }

    public Mono<InputStreamResource> getPhoto(String tcad) {
        return ldapService.getUser(tcad)
                .map(LdapUser::getThumbnailPhoto)
                .filter(Objects::nonNull)
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    public Mono<List<RoleType>> getAllRoles() {
        return Mono.just(Arrays.asList(RoleType.values()));
    }

    private Mono<Context> getUserEntity(Context context) {
        return userRepository.findById(context.tcad)
                .map(context::addUser)
                .defaultIfEmpty(context);
    }

    private Mono<Context> getRetailerForUser(Context context) {
        if(context.user == null) {
            return Mono.just(context);
        }

        return retailerRepository.findById(context.user.getRetailerId())
                .map(context::addRetailer)
                .defaultIfEmpty(context);
    }

    private Mono<Context> getLdapUser(Context context) {
        return ldapService.getUser(context.tcad)
                .flatMap(ldapUser -> {
                    final Mono<List<LdapGroup>> groupsRequest = Flux.fromIterable(ldapUser.getAuthorityGroups())
                            .filter(this::isAugustusGroup)
                            .flatMap(this::getGroup)
                            .collectList();

                    return groupsRequest.map(groups -> {
                        Set<String> ldapGroups = groups.stream()
                                .filter(group -> group.getMemberOf() != null)
                                .flatMap(group -> group.getMemberOf().stream())
                                .collect(Collectors.toSet());

                        return context.addLdapUser(ldapUser).addLdapGroups(new ArrayList<>(ldapGroups));
                    });
                });
    }

    private Mono<Context> getAuthorizedRoles(Context context) {
        if(context.ldapUser == null) {
            return Mono.just(context);
        }

        return Flux.fromIterable(context.ldapUser.getAuthorityGroups())
                .filter(this::isOurTeliaRole)
                .flatMap(this::getGroup)
                .map(this::toRoleType)
                .collect(Collectors.toList())
                .map(context::addAuthorizedRoles);
    }

    private Mono<Context> validateRoles(Context context) {
        return getAuthorizedRoles(context)
                .map(uw -> {
                    // Remove this check when roles are deployed and ready
                    if(!validateRoles) {
                        return context;
                    }

                    List<RoleType> authorizedRoles = context.authorizedRoles;

                    RoleType userRole = context.user.getRole();
                    if(!authorizedRoles.contains(userRole)) {
                        throw new BadRequestException("User does not have permission for role " + userRole);
                    }

                    RoleType retailerRole = context.retailer.getRole();
                    if(!authorizedRoles.contains(retailerRole)) {
                        throw new BadRequestException("User does not have permission for retailer " + context.retailer.getId() + " with role " + retailerRole);
                    }

                    return context;
                });
    }

    private RoleType toRoleType(LdapGroup ldapGroup) {
        String ldapRoleName = ldapGroup.getName().toLowerCase();

        return Arrays.stream(RoleType.values())
                .filter(roleType -> isOurTeliaAccessRole(ldapRoleName, roleType))
                .findFirst()
                .orElseThrow(() -> new InternalServerErrorException("Could not map role from value " + ldapRoleName));
    }

    private boolean isOurTeliaAccessRole(String ldapRoleName, RoleType roleType) {
        return ldapRoleName.contains(makeRoleIdIdm(roleType).toLowerCase()) ||
                ldapRoleName.contains(makeRoleIdTiga(roleType).toLowerCase());
    }

    private String makeRoleIdIdm(RoleType roleName) {
        return rolePrefixIdm + "_" + roleName;
    }

    private String makeRoleIdTiga(RoleType roleName) {
        return rolePrefixTiga + "_" + roleName;
    }

    // If field role or retailerId is missing when saving user, try to get them from the db
    private Mono<Context> setFieldsIfMissingInUpdate(Context context) {
        boolean missingRetailerId = context.user.getRetailerId() == null;
        boolean missingRole = context.user.getRole() == null;
        if(missingRole || missingRetailerId) {
            return userRepository.findById(context.tcad)
                    .switchIfEmpty(Mono.error(new InternalServerErrorException("Missing required field role or retailerId for user " + context.tcad)))
                    .map(userEntity -> {
                        if(missingRole) {
                            context.user.setRole(userEntity.getRole());
                        }
                        if(missingRetailerId) {
                            context.user.setRetailerId(userEntity.getRetailerId());
                        }
                        return context;
                    });
        }
        return Mono.just(context);
    }

    private UserResponse makeUserResponse(Context context) {
        UserResponse userResponse = new UserResponse();

        userResponse.setTcad(context.tcad);

        if(context.user != null) {
            userResponse.setRole(RoleConverter.convert(context.user.getRole()));
        }
        userResponse.setRetailer(RetailerConverter.convert(context.retailer));

        if(context.ldapUser != null) {
            userResponse.setFirstName(context.ldapUser.getFirstName());
            userResponse.setLastName(context.ldapUser.getLastName());
            userResponse.setDepartment(context.ldapUser.getDepartment());
        }

        userResponse.setGroups(context.ldapGroups);

        List<Role> authorizedRoles = context.authorizedRoles.stream()
                .map(RoleConverter::convert)
                .collect(Collectors.toList());

        userResponse.setAuthorizedRoles(authorizedRoles);

        // Remove this check when roles are deployed and ready
        if(Boolean.TRUE.equals(validateRoles)) {
            // Remove role if the user is not authorized to have it.
            // This can occur if the user has lost its role in idm.
            if(!authorizedRoles.contains(userResponse.getRole())) {
                userResponse.setRole(null);
            }

            if(userResponse.getRetailer() != null && !authorizedRoles.contains(userResponse.getRetailer().getRole())) {
                userResponse.setRetailer(null);
            }
        }

        return userResponse;
    }

    private Mono<LdapGroup> getGroup(String group) {
        return ldapGroupCache.getLdapGroup(group)
                .switchIfEmpty(ldapService.getGroup(group))
                .flatMap(ldapGroup -> ldapGroupCache.putLdapGroup(group, ldapGroup))
                .onErrorResume(e -> {
                    LOG.warn("Could not get group details for {}", group, e);
                    return Mono.empty();
                });
    }

    private boolean isAugustusGroup(String group) {
        return group.toLowerCase(Locale.ROOT).contains("augustus");
    }

    private boolean isOurTeliaRole(String group) {
        return group.toLowerCase(Locale.ROOT).contains(rolePrefixIdm.toLowerCase(Locale.ROOT)) ||
                group.toLowerCase(Locale.ROOT).contains(rolePrefixTiga.toLowerCase(Locale.ROOT));
    }

    private Mono<Context> saveUser(Context context) {
        return userRepository.save(context.user)
                .map(context::addUser);
    }

    private static class Context {
        private final String tcad;
        private UserEntity user;
        private RetailerEntity retailer;
        private LdapUser ldapUser;
        private List<String> ldapGroups;
        private List<RoleType> authorizedRoles;

        public Context(String tcad) {
            this.tcad = tcad;
        }

        public Context addUser(UserEntity userEntity) {
            this.user = userEntity;
            return this;
        }

        public Context addRetailer(RetailerEntity retailer) {
            this.retailer = retailer;
            return this;
        }

        public Context addLdapUser(LdapUser ldapUser) {
            this.ldapUser = ldapUser;
            return this;
        }

        public Context addLdapGroups(List<String> ldapGroups) {
            this.ldapGroups = ldapGroups;
            return this;
        }

        public Context addAuthorizedRoles(List<RoleType> authorizedRoles) {
            this.authorizedRoles = authorizedRoles;
            return this;
        }
    }
}
