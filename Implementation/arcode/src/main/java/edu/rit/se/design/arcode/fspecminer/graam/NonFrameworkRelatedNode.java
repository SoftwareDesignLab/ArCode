/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class NonFrameworkRelatedNode implements DirectedGraphNode {

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public DirectedGraphNode clone() {
        return null;
    }
}
