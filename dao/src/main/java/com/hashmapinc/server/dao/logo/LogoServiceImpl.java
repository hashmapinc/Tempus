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



package com.hashmapinc.server.dao.logo;

import com.hashmapinc.server.dao.logo.LogoDao;
import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.dao.entity.AbstractEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class LogoServiceImpl extends AbstractEntityService implements LogoService {

    @Autowired
    private LogoDao logoDao;

    @Override
    public Logo find() {
        return logoDao.find().get(0);
    }

    @Override
    public Logo saveLogo(Logo logo) {
        Logo logoNew =  logoDao.save(logo);
        if(logoNew != null) {
            return logoNew;
        }
        return null;
    }


    @Override
    public void deleteLogoById (String id) {

        Logo logo = logoDao.findById(id);

        if (logo != null) {

            logoDao.removeById(logo.getUuidId());
        }
    }
}
