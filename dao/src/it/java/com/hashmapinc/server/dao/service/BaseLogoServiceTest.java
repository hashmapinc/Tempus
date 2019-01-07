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

package com.hashmapinc.server.dao.service;

import com.hashmapinc.server.common.data.Logo;
import com.datastax.driver.core.utils.UUIDs;
import com.hashmapinc.server.common.data.UUIDConverter;
import com.hashmapinc.server.common.data.id.LogoId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Optional;


public class BaseLogoServiceTest extends AbstractServiceTest {

    @Test
    public void saveLogo() throws Exception {

        byte[] aByteArray = {0xa,0x2,0xf,(byte)0xff,(byte)0xff,(byte)0xff};

        Logo logo = new Logo();
        logo.setName("test.jpg");
        logo.setDisplay(true);
        logo.setFile(aByteArray);
        logoService.saveLogo(logo);

        List <Logo> logoNew = logoService.find();

        Logo logoId = logoNew.get(0);
        Assert.assertEquals(1, logoNew.size());

        logoService.deleteLogoByName(logoId.getName());

    }
}
