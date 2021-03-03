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

package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class GraphEditDistanceInfo {
    GRAAM sourceGraam;
    SubFSpec distSubFSpec;
    List<GraphEditOperation> operations;
    int distance;

    public GraphEditDistanceInfo(GRAAM sourceGraam, SubFSpec distSubFSpec) {
        this.sourceGraam = sourceGraam;
        this.distSubFSpec = distSubFSpec;
        operations = new ArrayList<>();
        distance = 0;
    }

    public int getDistance() {
        return distance;
    }

    public void addOperation(GraphEditOperation operation ){
        operations.add( operation );
        distance += operation.getCost();
    }

    public String getDescription(){
        String operationSequence = "";
        for (GraphEditOperation operation : operations) {
            operationSequence += operation.getDescription() + "\n";
        }
        return operationSequence;
    }

/*
    public String generateCode() throws ClassHierarchyException, IOException, CodeGenerationException {
        String classHierarchyExclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
        String frameworkJarFile = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";

        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarFile, classHierarchyExclusionFile);
        CodeGenerator codeGenerator = new CodeGenerator(classHierarchyUtil);

        FSpecEndNode fSpecEndNode = null;
        for (FSpecNode fSpecNode : distSubFSpec.getNodeSet()) {
            if( fSpecNode instanceof FSpecEndNode ) {
                fSpecEndNode = (FSpecEndNode) fSpecNode;
                break;
            }
        }

        return codeGenerator.generateCode( "CodeSuggestion" , distSubFSpec, fSpecEndNode).toString();
    }
*/

    public SubFSpec getDistSubFSpec() {
        return distSubFSpec;
    }
}
