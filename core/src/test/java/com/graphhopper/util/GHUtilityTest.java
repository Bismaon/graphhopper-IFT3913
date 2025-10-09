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
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.weighting.SpeedWeighting;
import com.graphhopper.storage.*;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testgetAdjnode() {
        try (BaseGraph graph = createGraph(null)) {
            EdgeIteratorState edge01 = graph.edge(0, 1).setDistance(10);
            EdgeIteratorState edge12 = graph.edge(1, 2).setDistance(10);
            EdgeIteratorState edge02 = graph.edge(0, 2).setDistance(20);

            int id01 = edge01.getEdge();
            int id12 = edge12.getEdge();
            int id02 = edge02.getEdge();

            // Correct
            assertEquals(1, GHUtility.getAdjNode(graph, id01, 1));
            assertEquals(0, GHUtility.getAdjNode(graph, id01, 0));

            // Incorrect
            assertThrows(NullPointerException.class, () -> {
                GHUtility.getAdjNode(graph, id12, 0); // from 1 → 2,node 0 does not exist so nullPointer
            });
            int invalidEdgeId = -1;
            assertEquals(1, GHUtility.getAdjNode(graph, invalidEdgeId, 1));  // from invalid edge id returns adjNode
        }
    }

    @Test
    public void testpathsEqualExceptOneEdge() {
        int source = 0;
        int target = 2;
        int distanceRef = 20;
        int distanceAlt =20;
        int timeRef = 10000;
        int timeAlt = 10000;
        int weightRef = 10;
        int weightAlt = 10;
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 10, 0.5, true);
        EncodingManager encodingManager = new EncodingManager.Builder().add(speedEnc).build();

        // Same distance, time, weight
        List<String> violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertTrue(violations.isEmpty(), "Equivalent paths should not produce violations");

        // Different length
        distanceRef=distanceAlt+10;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertFalse(violations.isEmpty(), "Different length paths should produce violations");
        distanceRef=10;// reset distance

        // Different time
        timeRef=timeAlt+100;
        violations = testPaths(encodingManager, distanceRef, weightRef, timeRef, source, target, weightAlt, distanceAlt, timeAlt);
        assertFalse(violations.isEmpty(), "Different time paths should produce violations");
        timeRef=timeAlt;// reset time

    }

    private List<String> testPaths(EncodingManager encodingManager, int distanceRef, int weightRef, int timeRef, int source, int target, int weightAlt, int distanceAlt, int timeAlt) {
        try (BaseGraph graph = createGraph(encodingManager)) {

            EdgeIteratorState edge01 = graph.edge(0, 1).setDistance((double) distanceRef/2);
            EdgeIteratorState edge12 = graph.edge(1, 2).setDistance((double) distanceRef/2);
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

    public BaseGraph createGraph(EncodingManager em) {
        if (em == null) {
            return new BaseGraph(
                    new RAMDirectory(),
                    false,
                    false,
                    128,
                    8).create(GRAPH_SIZE);
        } else {
            return new BaseGraph.Builder(em).create();
        }
    }
}

