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
package com.hashmapinc.server.common.data;

import com.hashmapinc.server.common.data.id.LogoId;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class Logo extends BaseData<LogoId> {

    private boolean display;
    private byte[] file;
    private String name;

    public Logo() {
        super();
    }


    public Logo(LogoId id) {
        super(id);
    }

    public Logo(Logo logo) {
        super(logo);
        this.file = logo.file;
        this.display = logo.display;
        this.name = logo.name;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "logo{" +
                "display=" + display +
                ", file=" + file +
                ",logoName=" + name +
                '}';
    }

}
