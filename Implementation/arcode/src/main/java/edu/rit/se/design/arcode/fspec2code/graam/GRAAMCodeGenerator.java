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

package edu.rit.se.design.arcode.fspec2code.graam;

import edu.rit.se.design.arcode.fspec2code.AbstractCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspec2code.TypeHolder;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class GRAAMCodeGenerator extends AbstractCodeGenerator<GRAAM, DirectedGraphNode> {

    public GRAAMCodeGenerator(ClassHierarchyUtil classHierarchyUtil) {
        super(classHierarchyUtil);
    }

    protected Pair getClassNameVarName(Object o, SymbolTable symbolTable) {
        String className = null;
        String varName = null;
        if (o instanceof FrameworkRelatedNode) {
            FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) o;

            if (frameworkRelatedNode.isInitNode() &&
                    (frameworkRelatedNode.isAbstractOrInterfaceConstructorNode() || !frameworkRelatedNode.isPublicMethod())) {
                className = getSimpeClassName(frameworkRelatedNode);
                varName = symbolTable.getSymbol(o);
            }
        } else if (o instanceof TypeHolder) {
            className = ((TypeHolder) o).getSimpleName();
            varName = symbolTable.getSymbol(o);
        }

        return new ImmutablePair(className, varName);
    }

    String getSimpeClassName(FrameworkRelatedNode frameworkRelatedNode) {
        return frameworkRelatedNode.getFrameworkRelatedClass().replaceAll(".*\\/", "");
    }

}
