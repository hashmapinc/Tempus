package com.hashmapinc.server.service.security.auth.rules;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.CustomerId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.Authority;
import com.hashmapinc.server.service.security.model.SecurityUser;
import com.hashmapinc.server.service.security.model.UserPrincipal;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = AttributeBasedPermissionEvaluatorTest.class, loader = SpringBootContextLoader.class)
@ComponentScan({"com.hashmapinc.server.service.security.auth.rules"})
public class AttributeBasedPermissionEvaluatorTest {

    @Autowired
    private AttributeBasedPermissionEvaluator evaluator;

    private SecurityUser admin;

    @Before
    public void setup(){
        this.admin = new SecurityUser(new UserId(UUIDs.timeBased()));
        this.admin.setEnabled(true);
        this.admin.setUserPrincipal(new UserPrincipal(UserPrincipal.Type.USER_NAME, "admin"));
        this.admin.setAuthority(Authority.SYS_ADMIN);
    }

    @Test
    public void testSystemAdminAccess(){
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(admin, null);
        boolean hasPermission = evaluator.hasPermission(authentication, new TestResource(), "ASSET_CREATE");

        Assert.assertTrue(hasPermission);
    }

    @Data
    public static class TestResource{
        private AssetId id;
        private TenantId tenantId;
        private CustomerId customerId;
    }
}
