/*
 * Copyright 2013 Cloudera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.cdk.data.spi;

import com.cloudera.cdk.data.Marker;
import com.cloudera.cdk.data.PartitionStrategy;
import com.cloudera.cdk.data.spi.MarkerRange.Boundary;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMarkerBoundary {
  private static Marker YEAR;
  private static Marker OCT;
  private static Marker OCT_12;
  private static Marker SEPT;
  private static Marker SEPT_30;
  private static Marker NOV;
  private static Marker NOV_1;
  private static PartitionStrategy strategy;
  private static MarkerComparator comparator;

  @BeforeClass
  public static void setup() {
    YEAR = new Marker.Builder("year", 2013).get();
    OCT = new Marker.Builder()
        .add("year", 2013)
        .add("month", 10)
        .get();
    OCT_12 = new Marker.Builder()
        .add("year", 2013)
        .add("month", 10)
        .add("day", 12)
        .get();
    SEPT = new Marker.Builder()
        .add("year", 2013)
        .add("month", 9)
        .get();
    SEPT_30 = new Marker.Builder()
        .add("year", 2013)
        .add("month", 9)
        .add("day", 30)
        .get();
    NOV = new Marker.Builder()
        .add("year", 2013)
        .add("month", 11)
        .get();
    NOV_1 = new Marker.Builder()
        .add("year", 2013)
        .add("month", 11)
        .add("day", 1)
        .get();
    strategy = new PartitionStrategy.Builder()
        .year("timestamp")
        .month("timestamp")
        .day("timestamp")
        .get();
    comparator = new MarkerComparator(strategy);
  }

  @Test
  public void testInclusiveLowerBound() {
    Boundary bound = new Boundary(comparator, OCT, true);
    Assert.assertFalse(bound.isLessThan(YEAR));
    Assert.assertFalse(bound.isLessThan(SEPT));
    Assert.assertFalse(bound.isLessThan(SEPT_30));
    Assert.assertTrue(bound.isLessThan(OCT_12));
    Assert.assertTrue(bound.isLessThan(OCT));
    Assert.assertTrue(bound.isLessThan(NOV_1));
    Assert.assertTrue(bound.isLessThan(NOV));
  }

  @Test
  public void testInclusiveUpperBound() {
    Boundary bound = new Boundary(comparator, OCT, true);
    Assert.assertFalse(bound.isGreaterThan(YEAR));
    Assert.assertTrue(bound.isGreaterThan(SEPT));
    Assert.assertTrue(bound.isGreaterThan(SEPT_30));
    Assert.assertTrue(bound.isGreaterThan(OCT_12));
    Assert.assertTrue(bound.isGreaterThan(OCT));
    Assert.assertFalse(bound.isGreaterThan(NOV_1));
    Assert.assertFalse(bound.isGreaterThan(NOV));
  }

  @Test
  public void testExclusiveLowerBound() {
    Boundary bound = new Boundary(comparator, OCT, false);
    Assert.assertFalse(bound.isLessThan(YEAR));
    Assert.assertFalse(bound.isLessThan(SEPT));
    Assert.assertFalse(bound.isLessThan(SEPT_30));
    Assert.assertFalse(bound.isLessThan(OCT_12));
    Assert.assertFalse(bound.isLessThan(OCT));
    Assert.assertTrue(bound.isLessThan(NOV_1));
    Assert.assertTrue(bound.isLessThan(NOV));
  }

  @Test
  public void testExclusiveUpperBound() {
    Boundary bound = new Boundary(comparator, OCT, false);
    Assert.assertFalse(bound.isGreaterThan(YEAR));
    Assert.assertTrue(bound.isGreaterThan(SEPT));
    Assert.assertTrue(bound.isGreaterThan(SEPT_30));
    Assert.assertFalse(bound.isGreaterThan(OCT_12));
    Assert.assertFalse(bound.isGreaterThan(OCT));
    Assert.assertFalse(bound.isGreaterThan(NOV_1));
    Assert.assertFalse(bound.isGreaterThan(NOV));
  }

  public void testUnbounded() {
    Boundary bound = Boundary.UNBOUNDED;
    Assert.assertTrue(bound.isGreaterThan(YEAR));
    Assert.assertTrue(bound.isGreaterThan(SEPT));
    Assert.assertTrue(bound.isGreaterThan(SEPT_30));
    Assert.assertTrue(bound.isGreaterThan(OCT_12));
    Assert.assertTrue(bound.isGreaterThan(OCT));
    Assert.assertTrue(bound.isGreaterThan(NOV_1));
    Assert.assertTrue(bound.isGreaterThan(NOV));
    Assert.assertTrue(bound.isLessThan(YEAR));
    Assert.assertTrue(bound.isLessThan(SEPT));
    Assert.assertTrue(bound.isLessThan(SEPT_30));
    Assert.assertTrue(bound.isLessThan(OCT_12));
    Assert.assertTrue(bound.isLessThan(OCT));
    Assert.assertTrue(bound.isLessThan(NOV_1));
    Assert.assertTrue(bound.isLessThan(NOV));
  }

}