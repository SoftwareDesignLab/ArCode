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

package edu.rit.se.design.arcode.fspec2code.subfspec;

import edu.rit.se.design.arcode.fspec2code.*;
import edu.rit.se.design.arcode.fspec2recom.SubFSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;


/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SubFSpecCodeGenerator extends AbstractCodeGenerator<FSpec, FSpecNode> {

    public SubFSpecCodeGenerator(ClassHierarchyUtil classHierarchyUtil ){
        super(classHierarchyUtil);
    }

    protected Pair getClassNameVarName(Object o, SymbolTable symbolTable ){
        String className = null;
        String varName = null;
        if( o instanceof FSpecNode){
            if( o instanceof FSpecAPIInstantiationNode &&
                    ( ((FSpecAPIInstantiationNode) o).isAbstractOrInterface() || !((FSpecAPIInstantiationNode) o).isPublicConstructor() )){
                className = ((FSpecAPIInstantiationNode) o).getSimpleClassName();
                varName = symbolTable.getSymbol( o );
            }
        }
        else if( o instanceof TypeHolder ){
            className = ((TypeHolder) o).getSimpleName();
            varName = symbolTable.getSymbol( o );
        }

        return new ImmutablePair( className, varName );
    }

}
