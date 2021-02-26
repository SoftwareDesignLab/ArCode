package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspec2code.graam.GRAAMCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.subfspec.SubFSpecCodeGenerator;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecEndNode;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecUtil;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecVisualizer;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class Test {


    public static void main(String[] args) throws Exception {

//        SpecMi

        String testGRAAMs = "/Users/as8308/Desktop/Ali/RIT/My Drive/Research/Projects/ArCode/DataRepository/JAAS/Test/SerializedGRAAMs";
        String fSpecFilePath =
                "/Users/as8308/Desktop/Ali/RIT/My Drive/Research/Projects/ArCode/DataRepository/JAAS/FSpec.spmn";
//                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/FSpec.spmn";


        String classHierarchyExclusionFile = "/Users/as8308/Desktop/Ali/RIT/My Drive/Research/Projects//NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
        String frameworkJarFile = "/Users/as8308/Desktop/Ali/RIT/My Drive/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";

        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarFile, classHierarchyExclusionFile);

 /*
         FSpec fSpec = FSpecUtil.loadFromFile( fSpecFilePath );
        System.out.println( new FSpecVisualizer( fSpec ).dotOutput());

         fSpec.iterator().forEachRemaining( fSpecNode -> {
            if( fSpecNode instanceof FSpecEndNode ){
                StringBuilder className = new StringBuilder("");
                Set<String> projectsName = new HashSet<>();
                fSpec.getPredNodes( fSpecNode ).forEach( fSpecNodePred -> {
                    projectsName.add( fSpec.getEdges( fSpecNodePred, fSpecNode ).iterator().next().getTitle() );
                } );
                projectsName.forEach( s -> className.append( className.length() == 0 ? s : ("_" + s) ) );
                SubFSpecCodeGenerator codeGenerator = new SubFSpecCodeGenerator(classHierarchyUtil);
                try {
                    System.out.println( codeGenerator.generateCode( className.toString() , "methodName", fSpec, (FSpecEndNode) fSpecNode) );
                } catch (CodeGenerationException e) {
                    e.printStackTrace();
                }
            }
        } );*/

        List<GRAAM> loadedTestGRAAMs = GRAAMBuilder.loadGRAAMsFromSerializedFolder( testGRAAMs );
        GRAAM testGRAAM = loadedTestGRAAMs.get(0);
        GRAAMCodeGenerator graamCodeGenerator = new GRAAMCodeGenerator( classHierarchyUtil );
        StringBuilder graam2Code = graamCodeGenerator.generateCode( testGRAAM.getTitle(), "methodName", testGRAAM, testGRAAM.getEndNode() );
        System.out.println( graam2Code );


    }
}

