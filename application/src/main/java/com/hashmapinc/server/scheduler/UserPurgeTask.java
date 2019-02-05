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

package com.hashmapinc.server.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
@Service
@Transactional
public class UserPurgeTask {

    @Value("${user.trial.expirationTime}")
    private  int expiryTimeInMinutes;

    @Value("${user.trial.reminderMailTime}")
    private int reminderMailTimeInMinutes;

    @Autowired
    private UserVerificationServiceImpl userVerificationService;

    @Scheduled(cron = "${user.trial.purgeCronExpression}")
    public void purgeExpiredUsers() {
        log.trace("Executing purgeExpiredUsers");
        userVerificationService.disableAllExpiredUser(expiryTimeInMinutes,reminderMailTimeInMinutes);
    }
}
