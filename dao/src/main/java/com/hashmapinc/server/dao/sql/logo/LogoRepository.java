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
import com.hashmapinc.server.dao.model.sql.LogoEntity;
import com.hashmapinc.server.dao.util.SqlDao;
import org.springframework.data.repository.CrudRepository;

@SqlDao
public interface LogoRepository extends CrudRepository<LogoEntity, String> {

    LogoEntity findById(String id);

    LogoEntity findByName(String name);
}
