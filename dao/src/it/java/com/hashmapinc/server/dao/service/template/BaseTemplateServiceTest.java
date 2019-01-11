/**
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
package com.hashmapinc.server.dao.service.template;

import com.hashmapinc.server.common.data.page.PaginatedResult;
import com.hashmapinc.server.common.data.template.TemplateMetadata;
import com.hashmapinc.server.dao.service.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public abstract class BaseTemplateServiceTest extends AbstractServiceTest {

    @Test
    public void saveTemplate() {
        TemplateMetadata givenTemplateMetadata = getTemplateMetadata();
        TemplateMetadata savedTemplateMetaData = templateService.save(givenTemplateMetadata);
        Assert.assertNotNull(savedTemplateMetaData.getId());

        TemplateMetadata fetchedTemplateMetadata = templateService.getTemplate(savedTemplateMetaData.getId());
        Assert.assertEquals(givenTemplateMetadata.getName(), fetchedTemplateMetadata.getName());
        Assert.assertEquals(givenTemplateMetadata.getBody(), fetchedTemplateMetadata.getBody());

        templateService.delete(savedTemplateMetaData.getId());
    }

    @Test
    public void deleteTemplate() {
        TemplateMetadata givenTemplateMetadata = getTemplateMetadata();
        TemplateMetadata savedTemplateMetadata = templateService.save(givenTemplateMetadata);
        Assert.assertNotNull(savedTemplateMetadata.getId());

        templateService.delete(savedTemplateMetadata.getId());
        TemplateMetadata templateMetadata = templateService.getTemplate(savedTemplateMetadata.getId());
        Assert.assertNull(templateMetadata);
    }

    @Test
    public void getTemplates() {
        TemplateMetadata givenTemplateMetadata1 = getTemplateMetadata();
        TemplateMetadata savedTemplateMetadata1 = templateService.save(givenTemplateMetadata1);
        Assert.assertNotNull(savedTemplateMetadata1.getId());
        TemplateMetadata givenTemplateMetadata2 = getTemplateMetadata();
        TemplateMetadata savedTemplateMetadata2 = templateService.save(givenTemplateMetadata2);
        Assert.assertNotNull(savedTemplateMetadata2.getId());
        TemplateMetadata givenTemplateMetadata3 = getTemplateMetadata();
        TemplateMetadata savedTemplateMetadata3 = templateService.save(givenTemplateMetadata3);
        Assert.assertNotNull(savedTemplateMetadata3.getId());

        List<TemplateMetadata> templateMetadataList = templateService.getAllTemplates();
        Assert.assertEquals(3, templateMetadataList.size());

        PaginatedResult<TemplateMetadata> templateMetadataPage = templateService.getTemplatesByPage(2, 1, "temp");
        Assert.assertEquals(1, templateMetadataPage.getData().size());
    }

    private TemplateMetadata getTemplateMetadata() {
        TemplateMetadata templateMetadata = new TemplateMetadata();
        templateMetadata.setName("templatename");
        templateMetadata.setBody("{templatebody}");
        return templateMetadata;
    }

}
