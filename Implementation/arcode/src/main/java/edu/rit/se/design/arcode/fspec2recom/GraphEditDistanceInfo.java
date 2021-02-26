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
