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
package com.hashmapinc.server.dao.unitconversion;

import com.hashmapinc.tempus.UnitConvertorContext;
import com.hashmapinc.tempus.exception.QuantityClassSetException;
import com.hashmapinc.tempus.exception.UnitConvertorContextException;
import com.hashmapinc.tempus.exception.UnitConvertorException;
import com.hashmapinc.tempus.model.Quantity;
import com.hashmapinc.tempus.service.QuantityClassSetService;
import com.hashmapinc.tempus.service.UnitConvertorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class UnitConversionServiceImpl implements UnitConversionService {

    private UnitConvertorService unitConvertorService;
    private QuantityClassSetService quantityClassSetService;

    @PostConstruct
    public void init() throws UnitConvertorContextException {
        unitConvertorService = UnitConvertorContext.getInstanceOfUnitConvertorService();
        quantityClassSetService = UnitConvertorContext.getInstanceOfQuantityClassSetService();
    }

    @Override
    public Quantity convertToSiUnit(Quantity quantity) {
        try {
            return unitConvertorService.convertToSiUnit(quantity);
        } catch (UnitConvertorException ex) {
            log.info(ex.getMessage());
        }
        return quantity;
    }

    @Override
    public Quantity convertToTargetUnit(Quantity quantity , String targetUnit) {
        try {
            return unitConvertorService.convertToTargetUnit(quantity, targetUnit);
        } catch (UnitConvertorException ex) {
            log.info(ex.getMessage());
        }
        return quantity;
    }

    @Override
    public Set<String> getAllQuantityClass() {
        try {
            return quantityClassSetService.getAllQuantityClass();
        } catch (QuantityClassSetException ex) {
            log.info(ex.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public Map<String, Set<String>> getMemberUnitsForAllQuantityClass() {
        try {
            return quantityClassSetService.getMemberUnitsForAllQuantityClass();
        } catch (QuantityClassSetException ex) {
            log.info(ex.getMessage());
        }
        return new HashMap<>();
    }

    @Override
    public Set<String> getMemberUnitsForQuantityClass(String quantityClass) {
        try {
            return quantityClassSetService.getMemberUnitsForQuantityClass(quantityClass);
        } catch (QuantityClassSetException ex) {
            log.info(ex.getMessage());
        }
        return Collections.emptySet();
    }
}
