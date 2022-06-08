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

public class NonFrameworkMiddleNode extends NonFrameworkRelatedNode{
    StatementRepresentation statementRepresentation;

    public NonFrameworkMiddleNode(StatementRepresentation statementRepresentation) {
        this.statementRepresentation = statementRepresentation;
    }

    @Override
    public String getTitle() {
        return statementRepresentation.getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonFrameworkMiddleNode that = (NonFrameworkMiddleNode) o;
        return statementRepresentation.equals( that.statementRepresentation );
    }

    @Override
    public int hashCode() {
        return Objects.hash(statementRepresentation);
    }

    @Override
    public NonFrameworkMiddleNode clone() {
        return new NonFrameworkMiddleNode( statementRepresentation );
    }
}
