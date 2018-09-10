package com.hashmapinc.server.service.security.auth.permissions;


import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.TempusResource;
import com.hashmapinc.server.common.data.UserPermission;
import com.hashmapinc.server.common.data.asset.Asset;
import com.hashmapinc.server.common.data.id.AssetId;
import com.hashmapinc.server.common.data.id.DataModelObjectId;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.dao.datamodel.DataModelObjectService;
import org.apache.commons.collections4.ListUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.hashmapinc.server.service.security.auth.permissions.PermissionMatcherTest.PERMISSION_TO_TEST.PERMISSION_TO_ACCESS;
import static com.hashmapinc.server.service.security.auth.permissions.PermissionMatcherTest.PERMISSION_TO_TEST.PERMISSION_TO_ACT;

@ActiveProfiles("permission-attr-test")
@RunWith(Parameterized.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = PermissionMatcherTest.class, loader = SpringBootContextLoader.class)
@ComponentScan({"com.hashmapinc.server.service.security.auth.permissions"})
public class PermissionMatcherTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CustomerUserPermissionMatcher customerUserPermissionMatcher;

    TempusResource tempusResource;
    private static AssetId assetId = new AssetId(UUIDs.timeBased());
    private static DataModelObjectId dataModelObjectId = new DataModelObjectId(UUIDs.timeBased());

    @Parameterized.Parameter(value = 0)
    public PERMISSION_TO_TEST permissionToTest;

    @Parameterized.Parameter(value = 1)
    public String userActionOrResourceType;

    @Parameterized.Parameter(value = 2)
    public String userPermissionExpression;

    @Parameterized.Parameter(value = 3)
    public boolean expectedResult;


    @Parameterized.Parameters(name = "{index}:{0} Action : {1}, Permission : {2}")
    public static Collection<Object[]> data() {
        List<Object[]> permissionToActCases = getPermissionToActCases();
        return ListUtils.union(permissionToActCases, getPermissionToAccessCases());
    }

    private static List<Object[]> getPermissionToActCases() {
        List<Object[]> positiveCases = Arrays.asList(new Object[][]{
                {PERMISSION_TO_ACT, "READ", "CUSTOMER_USER:*:READ", true},
                {PERMISSION_TO_ACT, "UPDATE", "CUSTOMER_USER:*:UPDATE", true},
                {PERMISSION_TO_ACT, "DELETE", "CUSTOMER_USER:*:DELETE", true},
                {PERMISSION_TO_ACT, "ASSIGN", "CUSTOMER_USER:*:ASSIGN", true},
                {PERMISSION_TO_ACT, "CREATE", "CUSTOMER_USER:*:CREATE", true},
                {PERMISSION_TO_ACT, "READ", "CUSTOMER_USER:*:*", true},
                {PERMISSION_TO_ACT, "READ", "CUSTOMER_USER:*:READ", true},
                {PERMISSION_TO_ACT, "UPDATE", "CUSTOMER_USER:*:*", true},
                {PERMISSION_TO_ACT, "DELETE", "CUSTOMER_USER:*:*", true},
                {PERMISSION_TO_ACT, "ASSIGN", "CUSTOMER_USER:*:*", true},
                {PERMISSION_TO_ACT, "CREATE", "CUSTOMER_USER:*:*", true},
                {PERMISSION_TO_ACT, "CREATE", "CUSTOMER_USER:ASSET:*", true}
        });

        List<Object[]> negativeCases = Arrays.asList(new Object[][]{
                {PERMISSION_TO_ACT, "UPDATE", "CUSTOMER_USER:*:READ", false}
        });

        return ListUtils.union(positiveCases, negativeCases);
    }

    private static List<Object[]> getPermissionToAccessCases() {
        List<Object[]> positiveCases = Arrays.asList(new Object[][]{
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:*:READ", true},
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:ASSET:UPDATE", true},
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:ASSET?id="+ assetId +"&dataModelId="+dataModelObjectId+":*", true}
        });

        List<Object[]> negativeCases = Arrays.asList(new Object[][]{
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:DEVICE:READ", false},
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:ASSET?id="+ new AssetId(UUIDs.timeBased()) +"&dataModelId="+dataModelObjectId+":*", false},
                {PERMISSION_TO_ACCESS, "ASSET", "CUSTOMER_USER:ASSET?id="+ assetId +"&dataModelId="+new DataModelObjectId(UUIDs.timeBased())+":*", false},
                {PERMISSION_TO_ACCESS, "DEVICE", "CUSTOMER_USER:ASSET?id="+ assetId +"&dataModelId="+new DataModelObjectId(UUIDs.timeBased())+":*", false}
        });


        return ListUtils.union(positiveCases, negativeCases);
    }

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        Asset asset = new Asset();
        asset.setId(assetId);
        asset.setName("My asset");
        asset.setType("default");
        asset.setDataModelObjectId(dataModelObjectId);
        asset.setTenantId(new TenantId(UUIDs.timeBased()));
        tempusResource = asset;


    }

    @Test
    public void testPermissionToAct(){
        if(permissionToTest.equals(PERMISSION_TO_ACT))
            Assert.assertEquals(expectedResult, customerUserPermissionMatcher.hasPermissionToAct(tempusResource, userActionOrResourceType, new UserPermission(userPermissionExpression)));
    }

    @Test
    public void testPermissionToAccessResource(){
        if(permissionToTest.equals(PERMISSION_TO_ACCESS))
            Assert.assertEquals(expectedResult, customerUserPermissionMatcher.hasAccessToResource(tempusResource, userActionOrResourceType, new UserPermission(userPermissionExpression), null));
    }

    public enum PERMISSION_TO_TEST {
        PERMISSION_TO_ACT, PERMISSION_TO_ACCESS
    }
}

@Profile("permission-attr-test")
@Configuration
class PermissionEvaluatorTestConfiguration {

    @Bean
    @Primary
    public DataModelObjectService dataModelObjectService () { return Mockito.mock(DataModelObjectService.class); }

}
