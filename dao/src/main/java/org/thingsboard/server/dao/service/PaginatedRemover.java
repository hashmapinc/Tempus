/**
 * Copyright © 2016-2018 Hashmap, Inc
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
package org.thingsboard.server.dao.service;

import org.thingsboard.server.common.data.id.IdBased;
import org.thingsboard.server.common.data.page.TextPageLink;

import java.util.List;
import java.util.UUID;

public abstract class PaginatedRemover<I, D extends IdBased<?>> {

    private static final int DEFAULT_LIMIT = 100;

    public void removeEntities(I id) {
        TextPageLink pageLink = new TextPageLink(DEFAULT_LIMIT);
        boolean hasNext = true;
        while (hasNext) {
            List<D> entities = findEntities(id, pageLink);
            for (D entity : entities) {
                removeEntity(entity);
            }
            hasNext = entities.size() == pageLink.getLimit();
            if (hasNext) {
                int index = entities.size() - 1;
                UUID idOffset = entities.get(index).getUuidId();
                pageLink.setIdOffset(idOffset);
            }
        }
    }

    protected abstract List<D> findEntities(I id, TextPageLink pageLink);

    protected abstract void removeEntity(D entity);

}
