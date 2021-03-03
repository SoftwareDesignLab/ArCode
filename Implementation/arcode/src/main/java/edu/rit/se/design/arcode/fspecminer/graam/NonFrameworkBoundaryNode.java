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

import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class NonFrameworkBoundaryNode extends NonFrameworkRelatedNode{
    public enum GraphBoundaryNodeType {START_NODE, END_NODE}
    GraphBoundaryNodeType type;
    public NonFrameworkBoundaryNode(GraphBoundaryNodeType type) {
        this.type = type;
    }

    @Override
    public String getTitle() {
        return type.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        NonFrameworkBoundaryNode that = (NonFrameworkBoundaryNode) o;
//        return type.equals( that.type );
        return false;
    }

    public GraphBoundaryNodeType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public NonFrameworkBoundaryNode clone() {
        return new NonFrameworkBoundaryNode( type );
    }



}
