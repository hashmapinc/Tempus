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
import com.hashmapinc.kubeless.InputParserUtility;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

public class InputParserUtilityTest {
    @Test
    public void validateCorrectJson() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr),true);
        } catch (IOException e) {

        }
    }

    @Test
    public void validateInCorrectJsonThrowsException() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676}";
        try {
            inputParserUtility.validateJson(jsonStr);
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(true);
            System.out.println("Error occured " + e.getMessage());
        }
    }

    @Test
    public void validateCorrectJsonWithDsAndTs() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\", \"ds\":\"3000\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr, Collections.EMPTY_LIST),false);
        } catch (IOException e) {

        }
    }

    @Test
    public void validateCorrectJsonWithAddedKey() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\", \"key\":\"val\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr, Collections.singletonList("key")),true);
        } catch (IOException e) {

        }
    }

    @Test
    public void validateCorrectJsonWithoutAddedKey() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr, Collections.singletonList("key")),false);
        } catch (IOException e) {

        }
    }
}
