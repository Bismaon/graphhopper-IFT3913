/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.*;
import org.junit.jupiter.api.Test;
import com.github.javafaker.Faker;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {
    final int GRAPH_SIZE = 128;
    final int SEED = 42;

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    private BaseGraph createGraph(EncodingManager em) {
        if (em == null) {
            return new BaseGraph(
                    new RAMDirectory(),
                    true,
                    false,
                    128,
                    8).create(GRAPH_SIZE);
        } else {
            return new BaseGraph.Builder(em).create();
        }
    }


    private List<String> testPaths(EncodingManager encodingManager, double distanceRef, double weightRef, int timeRef, int source, int target, double weightAlt, double distanceAlt, int timeAlt) {
        try (BaseGraph graph = createGraph(encodingManager)) {

            EdgeIteratorState edge01 = graph.edge(0, 1).setDistance(distanceRef / 2.0);
            EdgeIteratorState edge12 = graph.edge(1, 2).setDistance(distanceRef / 2.0);
            EdgeIteratorState edge02 = graph.edge(0, 2).setDistance(distanceAlt);

            int id01 = edge01.getEdge();
            int id12 = edge12.getEdge();
            int id02 = edge02.getEdge();

            Path refPath = new Path(graph);
            refPath.setWeight(weightRef).setDistance(distanceRef).setTime(timeRef);
            refPath.addEdge(id01);
            refPath.addEdge(id12);
            refPath.setFromNode(source).setEndNode(target);

            Path altPath = new Path(graph);
            altPath.setWeight(weightAlt).setDistance(distanceAlt).setTime(timeAlt);
            altPath.addEdge(id02);
            altPath.setFromNode(source).setEndNode(target);

            return GHUtility.comparePaths(refPath, altPath, source, target, SEED);
        }
    }

    private void testPathsDifferentGraphs(EncodingManager encodingManager,
                                          double distanceRef, double weightRef, int timeRef,
                                          int source, int target,
                                          double weightAlt, double distanceAlt, int timeAlt) {
        try (BaseGraph graphA = createGraph(encodingManager);
             BaseGraph graphB = createGraph(encodingManager)) {

            EdgeIteratorState e01 = graphA.edge(0, 1).setDistance(distanceRef / 2.0);
            EdgeIteratorState e12 = graphA.edge(1, 2).setDistance(distanceRef / 2.0);
            int id01 = e01.getEdge(), id12 = e12.getEdge();

            EdgeIteratorState e02 = graphB.edge(0, 2).setDistance(distanceAlt);
            int id02 = e02.getEdge();

            Path refPath = new Path(graphA).setWeight(weightRef).setDistance(distanceRef).setTime(timeRef);
            refPath.addEdge(id01);
            refPath.addEdge(id12);
            refPath.setFromNode(source).setEndNode(target);

            Path altPath = new Path(graphB).setWeight(weightAlt).setDistance(distanceAlt).setTime(timeAlt);
            altPath.addEdge(id02);
            altPath.setFromNode(source).setEndNode(target);

            GHUtility.comparePaths(refPath, altPath, source, target, SEED);
        }
    }

    @Test
    public void testGetAdjnode() {
        try (BaseGraph graph = createGraph(null)) {
            EdgeIteratorState edge01 = graph.edge(0, 1).setDistance(10);
            EdgeIteratorState edge12 = graph.edge(1, 2).setDistance(10);
            EdgeIteratorState edge02 = graph.edge(0, 2).setDistance(20);

            int id01 = edge01.getEdge();
            int id12 = edge12.getEdge();

            // Correct
            assertEquals(1, GHUtility.getAdjNode(graph, id01, 1), "Node `0` is adjacent to node `1`.");
            assertEquals(0, GHUtility.getAdjNode(graph, id01, 0), "Node `1` is adjacent to node `0`.");

            // Incorrect
            assertThrows(NullPointerException.class, () -> {
                GHUtility.getAdjNode(graph, id12, 0);
            }, "Node `0` does not exist on the edge and should throw an exception.");
            int invalidEdgeId = -1;
            assertEquals(1, GHUtility.getAdjNode(graph, invalidEdgeId, 1),
                    "Node `1` does not exist on the invalid edge and should return the adjNode specified.");
        }
    }

    @Test
    public void testPathsEqualExceptOneEdge() {
        int source = 0;
        int target = 2;
        double distanceRef = 20.0;
        double distanceAlt = 20.0;
        int timeRef = 10000;
        int timeAlt = 10000;
        double weightRef = 10.0;
        double weightAlt = 10.0;
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 10, 0.5, true);
        EncodingManager encodingManager = new EncodingManager.Builder().add(speedEnc).build();

        // Same or approximate distance, time, weight
        List<String> violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertTrue(violations.isEmpty(), "Equivalent paths should not produce violations.");

        distanceAlt = distanceRef + 0.9e-1;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertTrue(violations.isEmpty(), "Distance difference is smaller than 1.e-1 should not produce violations.");
        distanceAlt = distanceRef;// reset distance

        timeRef = timeAlt + 50;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertTrue(violations.isEmpty(), "Time difference is smaller than 50 should not produce violations.");
        timeRef = timeAlt;// reset time

        weightRef = weightAlt + 1e-2;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertTrue(violations.isEmpty(), "Weight difference is smaller than 1.e-2 should not produce violations.");
        weightRef = weightAlt;// reset weight

        // Different length
        distanceRef = distanceAlt + 10;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertFalse(violations.isEmpty(), "Different length paths should produce violations.");
        assertTrue(violations.get(0).contains("wrong distance"));
        distanceRef = distanceAlt;// reset distance

        // Different time
        timeRef = timeAlt + 51;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertFalse(violations.isEmpty(), "Different time paths should produce violations.");
        assertTrue(violations.get(0).contains("wrong time"));
        timeRef = timeAlt; // reset time

        // Different weight
        double finalDistanceRef = distanceRef;
        double finalDistanceAlt = distanceAlt;
        double finalWeightRef = weightAlt + 2.e-2;
        int finalTimeRef = timeRef;
        assertThrows(AssertionError.class, () -> {
            testPaths(encodingManager, finalDistanceRef, finalWeightRef, finalTimeRef, source, target, weightAlt, finalDistanceAlt, timeAlt);
        }, "Different weight paths should produce fail.");

        // Different Graphs
        assertThrows(AssertionError.class, () -> {
            testPathsDifferentGraphs(encodingManager, finalDistanceRef, finalWeightRef, finalTimeRef,
                    source, target, weightAlt, finalDistanceAlt, timeAlt);
        }, "Different graph structure should trigger AssertionError.");

    }

    @Test
    public void testGetCommonNode() {
        try (BaseGraph graph = createGraph(null)) {
            EdgeIteratorState edge01 = graph.edge(0, 1).setDistance(10);
            EdgeIteratorState edge12 = graph.edge(1, 2).setDistance(10);
            EdgeIteratorState edge02 = graph.edge(0, 2).setDistance(20);
            EdgeIteratorState edge23 = graph.edge(2, 3).setDistance(20);

            int id01 = edge01.getEdge();
            int id12 = edge12.getEdge();
            int id02 = edge02.getEdge();
            int id23 = edge23.getEdge();

            // valid
            assertEquals(1, GHUtility.getCommonNode(graph, id01, id12), "the common node `1` should be returned.");
            assertEquals(0, GHUtility.getCommonNode(graph, id01, id02), "the common node `0` should be returned.");

            // no common node
            assertThrows(IllegalArgumentException.class, () -> {
                GHUtility.getCommonNode(graph, id01, id23);
            }, "Edges not sharing any nodes should return an Exception.");

            // form a circle
            assertThrows(IllegalArgumentException.class, () -> {
                GHUtility.getCommonNode(graph, id01, id01);
            }, "Edges forming a circle should return an Exception.");
        }
    }

    @Test
    public void testGetProblems() {
        try (BaseGraph graph = createGraph(null)) {
            NodeAccess na = graph.getNodeAccess();

            // valid
            na.setNode(0, 89, -150, 10);
            na.setNode(1, 89, -179, 10);
            na.setNode(2, 80, -150, 10);
            na.setNode(3, 89, 179, 10);

            List<String> problems = GHUtility.getProblems(graph);
            assertTrue(problems.isEmpty(), "No problems should be detected.");

            // 5 problems
            na.setNode(0, 91, -150, 10);
            na.setNode(1, 91, -181, 10);
            na.setNode(2, 80, -150, 10);
            na.setNode(3, -91, 190, 10);

            problems = GHUtility.getProblems(graph);
            assertFalse(problems.isEmpty(), "Problems should be detected.");
            assertEquals(5, problems.size(), "5 problems should be detected.");
            assertTrue(problems.get(0).contains("latitude"));
            assertTrue(problems.get(1).contains("latitude"));
            assertTrue(problems.get(2).contains("longitude"));
            assertTrue(problems.get(3).contains("latitude"));
            assertTrue(problems.get(4).contains("longitude"));

        }
    }


    @Test
    public void testGetDistanceBetweenNodes() {
        try (BaseGraph graph = createGraph(null)) {
            NodeAccess na = graph.getNodeAccess();

            // Set node coordinates (approx Paris & London)
            na.setNode(0, 48.8566, 2.3522, 10);
            na.setNode(1, 51.5074, -0.1278, 10);

            // Hmmmm
            na.setNode(2 ,-190.2345, -191.3522, 10);

            double dist = GHUtility.getDistance(0, 1, na);

            // Expected distance between those coordinates
            assertTrue(dist > 300_000 && dist < 350_000);

            // Same node -> distance should be 0
            double distSame = GHUtility.getDistance(0, 0, na);
            assertEquals(0.0, distSame, 1e-6, "Distance between same nodes should be 0");

            // 12742 km being the greatest possible distance between 2 points on earth
            // Making sure the method doesnt try to normalise invalid values and give something
            // coherent
            double distInvalid = GHUtility.getDistance(0, 2, na);
            assertTrue(distInvalid > 12742000);
        }
    }

    @Test
    public void testGetEdge() {
        try(BaseGraph graph = createGraph(null)) {
            graph.edge(0, 1).setDistance(10);
            graph.edge(1, 2).setDistance(10);
            graph.edge(0, 2).setDistance(20);
            graph.edge(2, 3).setDistance(20);

            // No edge should return null
            assertNull(GHUtility.getEdge(graph, 0, 3));

            // Single edge should return non-null
            EdgeIteratorState edge = GHUtility.getEdge(graph, 0, 1);
            assertNotNull(edge);

            // Multiple edges should throw IllegalArgumentException saying:
            // "There are multiple edges between nodes 0 and 1"
            graph.edge(0,1).setDistance(25);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> GHUtility.getEdge(graph, 0, 1));
            assertTrue(ex.getMessage().contains("multiple edges"));
        }
    }

    @Test
    public void testSetSpeed() {
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 10, 0.5, true);
        BooleanEncodedValue accessEnc = new SimpleBooleanEncodedValue("access", true);
        EncodingManager em = new EncodingManager.Builder().add(speedEnc).add(accessEnc).build();

        try (BaseGraph graph = createGraph(em)) {
            // Forward
            EdgeIteratorState edge = graph.edge(0, 1);

            GHUtility.setSpeed(60.0, true, false, accessEnc, speedEnc, edge);

            assertTrue(edge.get(accessEnc));
            assertEquals(60.0, edge.get(speedEnc), 0.001);

            // Backwards
            GHUtility.setSpeed(40.0, false, true, accessEnc, speedEnc, edge);

            assertFalse(edge.get(accessEnc));
            assertEquals(40.0, edge.getReverse(speedEnc), 0.001);

            // No speed
            assertThrows(IllegalStateException.class, () -> {
                GHUtility.setSpeed(0.0, true, false, accessEnc, speedEnc, edge);
            });
        }
    }

    @Test
    public void testCreateCircleWithFaker() {
        Faker faker = new Faker();

        // Generate random ID and center coordinates
        String circleId = faker.bothify("circle-###");
        double centerLat = faker.number().randomDouble(6, -90, 90);
        double centerLon = faker.number().randomDouble(6, -180, 180);
        double radius = faker.number().randomDouble(2, 1, 1000);

        var circleFeature = GHUtility.createCircle(circleId, centerLat, centerLon, radius);

        assertNotNull(circleFeature);
        assertEquals(circleId, circleFeature.getId());

        // Check that the geometry is not null and contains the approximate center
        var coords = circleFeature.getGeometry().getCoordinates();
        assertTrue(coords.length > 0);
        boolean containsCenter = false;
        for (var coord : coords) {
            if (Math.abs(coord.y - centerLat) < 1.0 && Math.abs(coord.x - centerLon) < 1.0) {
                containsCenter = true;
                break;
            }
        }
        assertTrue(containsCenter);
    }
}

