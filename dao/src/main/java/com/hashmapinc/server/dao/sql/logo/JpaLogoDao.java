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


package com.hashmapinc.server.dao.sql.logo;

import com.hashmapinc.server.common.data.Logo;
import com.hashmapinc.server.dao.DaoUtil;
import com.hashmapinc.server.dao.model.sql.LogoEntity;
import com.hashmapinc.server.dao.sql.JpaAbstractDao;
import com.hashmapinc.server.dao.logo.LogoDao;
import com.hashmapinc.server.dao.util.SqlDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;


@Component
@SqlDao
@Slf4j


public class JpaLogoDao extends JpaAbstractDao<LogoEntity,Logo> implements LogoDao {

    @Autowired
    private LogoRepository logoRepository;

    @Override
    protected Class<LogoEntity> getEntityClass() {
        return LogoEntity.class;
    }

    @Override
    protected CrudRepository<LogoEntity, String> getCrudRepository() {
        return logoRepository;
    }

    @Override
    public Logo findById(String id) {
        Logo logo = DaoUtil.getData(logoRepository.findById(id));
        if (logo != null) {
            return logo;
        } else {
            return null;
        }
    }


    @Override
    public Logo findByName(String name) {
        Logo logo = DaoUtil.getData(logoRepository.findByName(name));
        if (logo != null) {
            return logo;
        } else {
            return null;
        }
    }



}
