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
package com.hashmapinc.server.service.install;

import com.hashmapinc.server.exception.TempusApplicationException;

public interface SystemDataLoaderService {

    void createSysAdminGroup() throws TempusApplicationException;

    void createSysAdmin() throws TempusApplicationException;

    void createAdminSettings() throws TempusApplicationException;

    void loadSystemWidgets() throws TempusApplicationException;

    void loadSystemPlugins() throws TempusApplicationException;

    void loadSystemRules() throws TempusApplicationException;

    void loadSystemThemes() throws TempusApplicationException;

    void loadDemoData() throws TempusApplicationException;

    void deleteSystemWidgetBundle(String bundleAlias) throws TempusApplicationException;

}
