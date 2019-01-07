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
package com.hashmapinc.server.service.security.auth.rules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.io.IOException;

public class SpelDeserializer extends StdDeserializer<Expression> {
    transient ExpressionParser parser = new SpelExpressionParser();

    public SpelDeserializer(){
        this(null);
    }

    protected SpelDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Expression deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        String expresionString = jp.getCodec().readValue(jp, String.class);
        return parser.parseExpression(expresionString);
    }
}
