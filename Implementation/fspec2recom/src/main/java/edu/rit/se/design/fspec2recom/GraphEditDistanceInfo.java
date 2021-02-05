package edu.rit.se.design.fspec2recom;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.fspec2code.CodeGenerationException;
import edu.rit.se.design.fspec2code.CodeGenerator;
import edu.rit.se.design.specminer.fspec.FSpecEndNode;
import edu.rit.se.design.specminer.fspec.FSpecNode;
import edu.rit.se.design.specminer.graam.GRAAM;

import java.io.IOException;
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

}
