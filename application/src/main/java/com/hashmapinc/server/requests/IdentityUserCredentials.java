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
package com.hashmapinc.server.requests;

import com.hashmapinc.server.common.data.id.UserCredentialsId;
import com.hashmapinc.server.common.data.id.UserId;
import com.hashmapinc.server.common.data.security.UserCredentials;
import lombok.Data;

import java.util.UUID;

@Data
public class IdentityUserCredentials {
    private UUID id;
    private UUID userId;
    private String password;
    private String activationToken;
    private String resetToken;


    public IdentityUserCredentials() {
    }

    public IdentityUserCredentials(UserCredentials userCredentials){
        this.id = userCredentials.getUuidId();
        this.userId = userCredentials.getUserId().getId();
        this.password = userCredentials.getPassword();
        this.activationToken = userCredentials.getActivateToken();
        this.resetToken = userCredentials.getResetToken();
    }



    public UserCredentials toUserCredentials(){
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setId(new UserCredentialsId(id));
        userCredentials.setUserId(new UserId(userId));
        userCredentials.setPassword(password);
        userCredentials.setActivateToken(activationToken);
        userCredentials.setResetToken(resetToken);
        return userCredentials;
    }
}
