/**
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hashmapinc.server.dao.service;

import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.id.TenantId;
import com.hashmapinc.server.common.data.page.TextPageData;
import com.hashmapinc.server.common.data.page.TextPageLink;
import com.hashmapinc.server.common.data.widget.WidgetsBundle;
import com.hashmapinc.server.dao.model.ModelConstants;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.hashmapinc.server.common.data.Tenant;
import com.hashmapinc.server.dao.exception.DataValidationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseWidgetsBundleServiceTest extends AbstractServiceTest {

    private IdComparator<WidgetsBundle> idComparator = new IdComparator<>();

    private TenantId tenantId;

    @Before
    public void before() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant");
        Tenant savedTenant = tenantService.saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
    }

    @After
    public void after() {
        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testSaveWidgetsBundle() throws IOException {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTenantId(tenantId);
        widgetsBundle.setTitle("My first widgets bundle");

        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);

        Assert.assertNotNull(savedWidgetsBundle);
        Assert.assertNotNull(savedWidgetsBundle.getId());
        Assert.assertNotNull(savedWidgetsBundle.getAlias());
        Assert.assertTrue(savedWidgetsBundle.getCreatedTime() > 0);
        Assert.assertEquals(widgetsBundle.getTenantId(), savedWidgetsBundle.getTenantId());
        Assert.assertEquals(widgetsBundle.getTitle(), savedWidgetsBundle.getTitle());

        savedWidgetsBundle.setTitle("My new widgets bundle");

        widgetsBundleService.saveWidgetsBundle(savedWidgetsBundle);
        WidgetsBundle foundWidgetsBundle = widgetsBundleService.findWidgetsBundleById(savedWidgetsBundle.getId());
        Assert.assertEquals(foundWidgetsBundle.getTitle(), savedWidgetsBundle.getTitle());

        widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
    }

    @Test(expected = DataValidationException.class)
    public void testSaveWidgetsBundleWithEmptyTitle() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTenantId(tenantId);
        widgetsBundleService.saveWidgetsBundle(widgetsBundle);
    }

    @Test(expected = DataValidationException.class)
    public void testSaveWidgetsBundleWithInvalidTenant() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTitle("My widgets bundle");
        widgetsBundle.setTenantId(new TenantId(UUIDs.timeBased()));
        widgetsBundleService.saveWidgetsBundle(widgetsBundle);
    }

    @Test(expected = DataValidationException.class)
    public void testUpdateWidgetsBundleTenant() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTitle("My widgets bundle");
        widgetsBundle.setTenantId(tenantId);
        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
        savedWidgetsBundle.setTenantId(new TenantId(ModelConstants.NULL_UUID));
        try {
            widgetsBundleService.saveWidgetsBundle(savedWidgetsBundle);
        } finally {
            widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
        }
    }

    @Test(expected = DataValidationException.class)
    public void testUpdateWidgetsBundleAlias() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTitle("My widgets bundle");
        widgetsBundle.setTenantId(tenantId);
        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
        savedWidgetsBundle.setAlias("new_alias");
        try {
            widgetsBundleService.saveWidgetsBundle(savedWidgetsBundle);
        } finally {
            widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
        }
    }

    @Test
    public void testFindWidgetsBundleById() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTenantId(tenantId);
        widgetsBundle.setTitle("My widgets bundle");
        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
        WidgetsBundle foundWidgetsBundle = widgetsBundleService.findWidgetsBundleById(savedWidgetsBundle.getId());
        Assert.assertNotNull(foundWidgetsBundle);
        Assert.assertEquals(savedWidgetsBundle, foundWidgetsBundle);
        widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
    }

    @Test
    public void testFindWidgetsBundleByTenantIdAndAlias() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTenantId(tenantId);
        widgetsBundle.setTitle("My widgets bundle");
        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
        WidgetsBundle foundWidgetsBundle = widgetsBundleService.findWidgetsBundleByTenantIdAndAlias(tenantId, savedWidgetsBundle.getAlias());
        Assert.assertNotNull(foundWidgetsBundle);
        Assert.assertEquals(savedWidgetsBundle, foundWidgetsBundle);
        widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
    }

    @Test
    public void testDeleteWidgetsBundle() {
        WidgetsBundle widgetsBundle = new WidgetsBundle();
        widgetsBundle.setTenantId(tenantId);
        widgetsBundle.setTitle("My widgets bundle");
        WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
        WidgetsBundle foundWidgetsBundle = widgetsBundleService.findWidgetsBundleById(savedWidgetsBundle.getId());
        Assert.assertNotNull(foundWidgetsBundle);
        widgetsBundleService.deleteWidgetsBundle(savedWidgetsBundle.getId());
        foundWidgetsBundle = widgetsBundleService.findWidgetsBundleById(savedWidgetsBundle.getId());
        Assert.assertNull(foundWidgetsBundle);
    }

    @Test
    public void testFindSystemWidgetsBundlesByPageLink() {

        TenantId tenantId = new TenantId(ModelConstants.NULL_UUID);

        List<WidgetsBundle> systemWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();
        List<WidgetsBundle> createdWidgetsBundles = new ArrayList<>();
        for (int i=0;i<235;i++) {
            WidgetsBundle widgetsBundle = new WidgetsBundle();
            widgetsBundle.setTenantId(tenantId);
            widgetsBundle.setTitle("Widgets bundle "+i);
            createdWidgetsBundles.add(widgetsBundleService.saveWidgetsBundle(widgetsBundle));
        }

        List<WidgetsBundle> widgetsBundles = new ArrayList<>(createdWidgetsBundles);
        widgetsBundles.addAll(systemWidgetsBundles);

        List<WidgetsBundle> loadedWidgetsBundles = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(19);
        TextPageData<WidgetsBundle> pageData = null;
        do {
            pageData = widgetsBundleService.findSystemWidgetsBundlesByPageLink(pageLink);
            loadedWidgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(widgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(widgetsBundles, loadedWidgetsBundles);

        for (WidgetsBundle widgetsBundle : createdWidgetsBundles) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }

        loadedWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        Collections.sort(systemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(systemWidgetsBundles, loadedWidgetsBundles);
    }

    @Test
    public void testFindSystemWidgetsBundles() {
        TenantId tenantId = new TenantId(ModelConstants.NULL_UUID);

        List<WidgetsBundle> systemWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        List<WidgetsBundle> createdWidgetsBundles = new ArrayList<>();
        for (int i=0;i<135;i++) {
            WidgetsBundle widgetsBundle = new WidgetsBundle();
            widgetsBundle.setTenantId(tenantId);
            widgetsBundle.setTitle("Widgets bundle "+i);
            createdWidgetsBundles.add(widgetsBundleService.saveWidgetsBundle(widgetsBundle));
        }

        List<WidgetsBundle> widgetsBundles = new ArrayList<>(createdWidgetsBundles);
        widgetsBundles.addAll(systemWidgetsBundles);

        List<WidgetsBundle> loadedWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        Collections.sort(widgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(widgetsBundles, loadedWidgetsBundles);

        for (WidgetsBundle widgetsBundle : createdWidgetsBundles) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }

        loadedWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        Collections.sort(systemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(systemWidgetsBundles, loadedWidgetsBundles);
    }

    @Test
    public void testFindTenantWidgetsBundlesByTenantId() {
        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();

        List<WidgetsBundle> widgetsBundles = new ArrayList<>();
        for (int i=0;i<127;i++) {
            WidgetsBundle widgetsBundle = new WidgetsBundle();
            widgetsBundle.setTenantId(tenantId);
            widgetsBundle.setTitle("Widgets bundle "+i);
            widgetsBundles.add(widgetsBundleService.saveWidgetsBundle(widgetsBundle));
        }

        List<WidgetsBundle> loadedWidgetsBundles = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(11);
        TextPageData<WidgetsBundle> pageData = null;
        do {
            pageData = widgetsBundleService.findTenantWidgetsBundlesByTenantId(tenantId, pageLink);
            loadedWidgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(widgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(widgetsBundles, loadedWidgetsBundles);

        widgetsBundleService.deleteWidgetsBundlesByTenantId(tenantId);

        pageLink = new TextPageLink(15);
        pageData = widgetsBundleService.findTenantWidgetsBundlesByTenantId(tenantId, pageLink);
        Assert.assertFalse(pageData.hasNext());
        Assert.assertTrue(pageData.getData().isEmpty());

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindAllWidgetsBundlesByTenantIdAndPageLink() {

        List<WidgetsBundle> systemWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();
        TenantId systemTenantId = new TenantId(ModelConstants.NULL_UUID);

        List<WidgetsBundle> createdWidgetsBundles = new ArrayList<>();
        List<WidgetsBundle> createdSystemWidgetsBundles = new ArrayList<>();
        for (int i=0;i<177;i++) {
            WidgetsBundle widgetsBundle = new WidgetsBundle();
            widgetsBundle.setTenantId(i % 2 == 0 ? tenantId : systemTenantId);
            widgetsBundle.setTitle((i % 2 == 0 ? "Widgets bundle " : "System widget bundle ") + i);
            WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
            createdWidgetsBundles.add(savedWidgetsBundle);
            if (i % 2 == 1) {
                createdSystemWidgetsBundles.add(savedWidgetsBundle);
            }
        }

        List<WidgetsBundle> widgetsBundles = new ArrayList<>(createdWidgetsBundles);
        widgetsBundles.addAll(systemWidgetsBundles);

        List<WidgetsBundle> loadedWidgetsBundles = new ArrayList<>();
        TextPageLink pageLink = new TextPageLink(17);
        TextPageData<WidgetsBundle> pageData = null;
        do {
            pageData = widgetsBundleService.findAllTenantWidgetsBundlesByTenantIdAndPageLink(tenantId, pageLink);
            loadedWidgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(widgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(widgetsBundles, loadedWidgetsBundles);

        widgetsBundleService.deleteWidgetsBundlesByTenantId(tenantId);

        loadedWidgetsBundles.clear();
        pageLink = new TextPageLink(14);
        do {
            pageData = widgetsBundleService.findAllTenantWidgetsBundlesByTenantIdAndPageLink(tenantId, pageLink);
            loadedWidgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        List<WidgetsBundle> allSystemWidgetsBundles = new ArrayList<>(systemWidgetsBundles);
        allSystemWidgetsBundles.addAll(createdSystemWidgetsBundles);

        Collections.sort(allSystemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(allSystemWidgetsBundles, loadedWidgetsBundles);

        for (WidgetsBundle widgetsBundle : createdSystemWidgetsBundles) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }

        loadedWidgetsBundles.clear();
        pageLink = new TextPageLink(18);
        do {
            pageData = widgetsBundleService.findAllTenantWidgetsBundlesByTenantIdAndPageLink(tenantId, pageLink);
            loadedWidgetsBundles.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageData.getNextPageLink();
            }
        } while (pageData.hasNext());

        Collections.sort(systemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(systemWidgetsBundles, loadedWidgetsBundles);

        tenantService.deleteTenant(tenantId);
    }

    @Test
    public void testFindAllWidgetsBundlesByTenantId() {

        List<WidgetsBundle> systemWidgetsBundles = widgetsBundleService.findSystemWidgetsBundles();

        Tenant tenant = new Tenant();
        tenant.setTitle("Test tenant");
        tenant = tenantService.saveTenant(tenant);

        TenantId tenantId = tenant.getId();
        TenantId systemTenantId = new TenantId(ModelConstants.NULL_UUID);

        List<WidgetsBundle> createdWidgetsBundles = new ArrayList<>();
        List<WidgetsBundle> createdSystemWidgetsBundles = new ArrayList<>();
        for (int i=0;i<277;i++) {
            WidgetsBundle widgetsBundle = new WidgetsBundle();
            widgetsBundle.setTenantId(i % 2 == 0 ? tenantId : systemTenantId);
            widgetsBundle.setTitle((i % 2 == 0 ? "Widgets bundle " : "System widget bundle ") + i);
            WidgetsBundle savedWidgetsBundle = widgetsBundleService.saveWidgetsBundle(widgetsBundle);
            createdWidgetsBundles.add(savedWidgetsBundle);
            if (i % 2 == 1) {
                createdSystemWidgetsBundles.add(savedWidgetsBundle);
            }
        }

        List<WidgetsBundle> widgetsBundles = new ArrayList<>(createdWidgetsBundles);
        widgetsBundles.addAll(systemWidgetsBundles);

        List<WidgetsBundle> loadedWidgetsBundles = widgetsBundleService.findAllTenantWidgetsBundlesByTenantId(tenantId);

        Collections.sort(widgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(widgetsBundles, loadedWidgetsBundles);

        widgetsBundleService.deleteWidgetsBundlesByTenantId(tenantId);

        loadedWidgetsBundles = widgetsBundleService.findAllTenantWidgetsBundlesByTenantId(tenantId);

        List<WidgetsBundle> allSystemWidgetsBundles = new ArrayList<>(systemWidgetsBundles);
        allSystemWidgetsBundles.addAll(createdSystemWidgetsBundles);

        Collections.sort(allSystemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(allSystemWidgetsBundles, loadedWidgetsBundles);

        for (WidgetsBundle widgetsBundle : createdSystemWidgetsBundles) {
            widgetsBundleService.deleteWidgetsBundle(widgetsBundle.getId());
        }

        loadedWidgetsBundles = widgetsBundleService.findAllTenantWidgetsBundlesByTenantId(tenantId);

        Collections.sort(systemWidgetsBundles, idComparator);
        Collections.sort(loadedWidgetsBundles, idComparator);

        Assert.assertEquals(systemWidgetsBundles, loadedWidgetsBundles);

        tenantService.deleteTenant(tenantId);
    }

}
