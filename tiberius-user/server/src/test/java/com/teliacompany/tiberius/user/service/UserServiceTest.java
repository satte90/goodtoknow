package com.teliacompany.tiberius.user.service;

import com.teliacompany.ldap.model.LdapGroup;
import com.teliacompany.ldap.model.LdapUser;
import com.teliacompany.ldap.service.LdapService;
import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsConfigurer;
import com.teliacompany.tiberius.base.test.utils.approvals.ApprovalsUtils;
import com.teliacompany.tiberius.user.api.v1.UserResponse;
import com.teliacompany.tiberius.user.cache.LdapGroupCache;
import com.teliacompany.tiberius.user.model.RetailerEntity;
import com.teliacompany.tiberius.user.model.RoleType;
import com.teliacompany.tiberius.user.model.UserEntity;
import com.teliacompany.tiberius.user.repository.RetailerRepository;
import com.teliacompany.tiberius.user.repository.UserRepository;
import com.teliacompany.webflux.error.exception.client.BadRequestException;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserServiceTest {

    public static final String MI6_RETAILER_ID = "TG9uZ0xpdmVUaGVRdWVlbg==";
    public static final String ROLE_PREFIX = "OURTELIA_TEST";

    private UserRepository userRepoMock;
    private RetailerRepository retailerRepoMock;
    private LdapService ldapServiceMock;
    private UserService userService;

    private RoleType userRole;
    private RoleType retailerRole;
    private List<RoleType> availableRoles;

    @BeforeEach
    public void before() {
        ApprovalsConfigurer.configure();

        if(userRepoMock != null && retailerRepoMock != null && ldapServiceMock != null) {
            Mockito.reset(userRepoMock, retailerRepoMock, ldapServiceMock);
        }

        userRepoMock = Mockito.mock(UserRepository.class);
        retailerRepoMock = Mockito.mock(RetailerRepository.class);
        ldapServiceMock = Mockito.mock(LdapService.class);

        availableRoles = new ArrayList<>();

        userService = new UserService(userRepoMock, retailerRepoMock, ldapServiceMock, new LdapGroupCacheMock(), ROLE_PREFIX, ROLE_PREFIX, "true");
    }

    @Test
    public void testGetUserSuccessful() throws InvalidNameException {
        userRole = RoleType.OURTELIA;
        retailerRole = RoleType.RETAILER;
        availableRoles.add(RoleType.OURTELIA);
        availableRoles.add(RoleType.RETAILER);
        mockAgent007();

        UserResponse user = userService.getUser("agent007").block();

        ApprovalsUtils.verifyJson(user,"service","testGetUserSuccessful");
    }

    @Test
    public void testGetUserSuccessfulWithNoAuthorizedRoles() throws InvalidNameException {
        userRole = RoleType.OURTELIA;
        retailerRole = RoleType.RETAILER;
        mockAgent007();

        UserResponse user = userService.getUser("agent007").block();

        ApprovalsUtils.verifyJson(user,"service","testGetUserSuccessfulWithNoAuthorizedRoles");
    }

    @Test
    public void testCreateUserSuccessful() throws InvalidNameException {
        userRole = RoleType.OURTELIA;
        retailerRole = RoleType.RETAILER;
        availableRoles.add(RoleType.OURTELIA);
        availableRoles.add(RoleType.RETAILER);
        UserEntity agent007Entity = mockAgent007();

        UserResponse user = userService.saveUser(agent007Entity).block();

        ApprovalsUtils.verifyJson(user,"service","testCreateUserSuccessful");
    }

    @Test
    public void testCreateUserWithUserRoleNotAvailable() throws InvalidNameException, NoSuchFieldException, IllegalAccessException {
        userRole = RoleType.OURTELIA;
        retailerRole = RoleType.RETAILER;
        availableRoles.add(RoleType.RETAILER);
        UserEntity agent007Entity = mockAgent007();

        BadRequestException badRequestException = Assertions.assertThrows(BadRequestException.class, userService.saveUser(agent007Entity)::block);
        removeStacktraceAndSuppressedFields(badRequestException);

        //Finally, we can verify with some consistency using Approvals
        ApprovalsUtils.verifyJson(badRequestException,"service","testCreateUserWithUserRoleNotAvailable");
    }

    @Test
    public void testCreateUserWithRetailerRoleNotAvailable() throws InvalidNameException, NoSuchFieldException, IllegalAccessException {
        userRole = RoleType.OURTELIA;
        retailerRole = RoleType.RETAILER;
        availableRoles.add(RoleType.OURTELIA);
        UserEntity agent007Entity = mockAgent007();

        BadRequestException badRequestException = Assertions.assertThrows(BadRequestException.class, userService.saveUser(agent007Entity)::block);
        removeStacktraceAndSuppressedFields(badRequestException);

        //Finally, we can verify with some consistency using Approvals
        ApprovalsUtils.verifyJson(badRequestException,"service","testCreateUserWithRetailerRoleNotAvailable");
    }

    private UserEntity mockAgent007() throws InvalidNameException {
        //Mock user entity, agent007, is returned when calling findById on userRepo with tcad agent007
        UserEntity agent007Entity = new UserEntity("agent007", userRole, MI6_RETAILER_ID);
        Mockito.doReturn(Mono.just(agent007Entity)).when(userRepoMock).findById(Mockito.eq("agent007"));
        Mockito.doReturn(Mono.just(agent007Entity)).when(userRepoMock).save(Mockito.eq(agent007Entity));

        //Mock retailer entity, MI6, is returned when calling findById on retailerId with MI6_RETAILER_ID
        RetailerEntity mi6Retailer = new RetailerEntity(MI6_RETAILER_ID, "MI6", retailerRole);
        Mockito.doReturn(Mono.just(mi6Retailer)).when(retailerRepoMock).findById(Mockito.eq(MI6_RETAILER_ID));

        //Mock retailer entity, MI6, is returned when calling findById on retailerId with MI6_RETAILER_ID
        Name name = new LdapName("dn=MI6");
        List<String> authorities = new ArrayList<>();
        LdapUser agent007LdapUser = new LdapUser(name, authorities, "the circus", null, "James", "Bond", "agent007", "agentDaddy@hotmail.com", "0777077007");
        Mockito.doReturn(Mono.just(agent007LdapUser)).when(ldapServiceMock).getUser(Mockito.eq("agent007"));

        LdapGroup group1 = new LdapGroup(new LdapName("dn=001"), "augustus On Her Majesty's Secret Service", Collections.singletonList("sis"));
        authorities.add("augustus On Her Majesty's Secret Service");
        Mockito.doReturn(Mono.just(group1)).when(ldapServiceMock).getGroup(Mockito.eq("augustus On Her Majesty's Secret Service"));

        LdapGroup group2 = new LdapGroup(new LdapName("dn=002"), "augustus Licence to kill", Collections.singletonList("sis"));
        authorities.add("auGusTus Licence to kill");
        Mockito.doReturn(Mono.just(group2)).when(ldapServiceMock).getGroup(Mockito.eq("auGusTus Licence to kill"));

        LdapGroup group3 = new LdapGroup(new LdapName("dn=003"), "augustus Casino Bouncer", Collections.singletonList("sissies"));
        authorities.add("Augustus Casino Bouncer");
        Mockito.doReturn(Mono.just(group3)).when(ldapServiceMock).getGroup(Mockito.eq("Augustus Casino Bouncer"));

        for (int i = 0; i < availableRoles.size(); i++) {
            String ldapRoleName = ROLE_PREFIX + "_" + availableRoles.get(i);
            LdapGroup roleGroup = new LdapGroup(new LdapName("dn=00" + (4 + i)), ldapRoleName, Collections.singletonList("ourtelia-role"));
            authorities.add(ldapRoleName);
            Mockito.doReturn(Mono.just(roleGroup)).when(ldapServiceMock).getGroup(Mockito.eq(ldapRoleName));
        }

        return agent007Entity;
    }

    private static void removeStacktraceAndSuppressedFields(BadRequestException badRequestException) throws NoSuchFieldException, IllegalAccessException {
        badRequestException.setStackTrace(new StackTraceElement[0]); // Don't care about stacktrace, it will differ between tests

        // Also remove the suppressed field (called suppressedExceptions in the Throwable supreclass for BadRequestException) to null using reflection
        Field suppressedField = Throwable.class.getDeclaredField("suppressedExceptions");
        suppressedField.setAccessible(true);
        suppressedField.set(badRequestException, null);
    }
}
