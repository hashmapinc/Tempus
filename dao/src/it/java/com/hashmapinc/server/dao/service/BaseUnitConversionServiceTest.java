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
package com.hashmapinc.server.dao.service;

import com.hashmapinc.tempus.model.Quantity;
import com.hashmapinc.tempus.service.UnitSystem;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;

public class BaseUnitConversionServiceTest extends AbstractServiceTest {

    @Test
    public void convertToSiUnitMeterShouldReturnMeter() {
        Quantity quantity = new Quantity(10.0, "m");
        Quantity siQuantity = unitConversionService.convertToSiUnit(quantity);
        Assert.assertNotNull(siQuantity);
        Assert.assertEquals(quantity, siQuantity);
    }

    @Test
    public void convertToSiUnitKiloMeter() {
        Quantity quantity = new Quantity(10.0, "km");
        Quantity siQuantity = unitConversionService.convertToSiUnit(quantity);
        Assert.assertNotNull(siQuantity);

        Assert.assertEquals(Double.valueOf(10000.0), siQuantity.getValue());
    }

    @Test
    public void convertToSiUnitDegCelcius() {
        Quantity quantity = new Quantity(10.0, "degC");
        Quantity siQuantity = unitConversionService.convertToSiUnit(quantity);
        Assert.assertNotNull(siQuantity);
        Assert.assertEquals(Double.valueOf(283.15), siQuantity.getValue());
    }

    @Test
    public void convertToTargetUnit() {
        Quantity quantity = new Quantity(10.0, "m");
        Quantity targetQuantity = unitConversionService.convertToTargetUnit(quantity, "km");
        Assert.assertNotNull(targetQuantity);
        Assert.assertEquals(Double.valueOf(0.01), targetQuantity.getValue());
    }

    @Test
    public void convertToTargetUnitMeterShouldReturnMeter() {
        Quantity quantity = new Quantity(10.0, "m");
        Quantity targetQuantity = unitConversionService.convertToTargetUnit(quantity, "m");
        Assert.assertNotNull(targetQuantity);
        Assert.assertEquals(quantity, targetQuantity);
    }

    @Test
    public void getAllQuantityClass() {
        Set quantitySetClasses = unitConversionService.getAllQuantityClass();
        Assert.assertNotNull(quantitySetClasses);
    }

    @Test
    public void getMemberUnitsForQuantityClassForLength() {
        Set lengths = unitConversionService.getMemberUnitsForQuantityClass("length");
        Assert.assertNotNull(lengths);
    }

    @Test
    public void getMemberUnitsForQuantityClassForMass() {
        Set masses = unitConversionService.getMemberUnitsForQuantityClass("mass");
        Assert.assertNotNull(masses);
    }

    @Test
    public void getUnitForDogLegAngleGradient() {
        Assert.assertEquals("deg/100ft", unitConversionService.getUnitFor("ENGLISH", "deg/30m"));
    }

    @Test
    public void getUnitForMassPerTimePerArea() {
        Assert.assertEquals("lbm/(ft2.s)", unitConversionService.getUnitFor("ENGLISH", "kg/(m2.s)"));
    }
}
