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
        // Création du graph et de sa population
        try (BaseGraph graph = createGraph(null)) {
            graph.edge(0, 1).setDistance(10);
            graph.edge(1, 2).setDistance(10);
            graph.edge(0, 2).setDistance(20);

            // Correct
            assertEquals(1, GHUtility.getAdjNode(graph, 0, 1));  // from edge 0-1, no0 → 1
            assertEquals(0, GHUtility.getAdjNode(graph, 0, 0));  // from 0 → 1

            // Incorrect
            assertThrows(NullPointerException.class, () -> {
                GHUtility.getAdjNode(graph, 1, 0); // from 1 → 0 edge does not exist so nullPointer
            });
            int invalidEdgeId = -1;
            assertEquals(1, GHUtility.getAdjNode(graph, invalidEdgeId, 1));  // from -1 → 1 invalid edge id returns adjNode
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

