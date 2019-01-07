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
package com.hashmapinc.server.controller.sql;

import com.hashmapinc.server.controller.BaseDataModelControllerTest;
import com.hashmapinc.server.dao.service.DaoSqlTest;
import com.hashmapinc.server.dao.sql.datamodel.DataModelObjectRespository;
import com.hashmapinc.server.dao.sql.datamodel.DataModelRepository;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

@DaoSqlTest
public class DataModelControllerSqlTest extends BaseDataModelControllerTest {

    @Autowired
    private DataModelRepository dataModelRepository;

    @Autowired
    private DataModelObjectRespository dataModelObjectRespository;

    @After
    public void afterTest(){
        dataModelObjectRespository.deleteAll();
        dataModelRepository.deleteAll();
    }

}
