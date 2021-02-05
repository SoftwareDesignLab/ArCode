package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecEndNode;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecUtil;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecVisualizer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class Test {


    public static void main(String[] args) throws Exception {

        String fSpecFilePath =
                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/FSpec.spmn";
//                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/FSpec.spmn";

        FSpec fSpec = FSpecUtil.loadFromFile( fSpecFilePath );

        System.out.println( new FSpecVisualizer( fSpec ).dotOutput());

        String classHierarchyExclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
        String frameworkJarFile = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";

        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarFile, classHierarchyExclusionFile);

        fSpec.iterator().forEachRemaining( fSpecNode -> {
            if( fSpecNode instanceof FSpecEndNode ){
                StringBuilder className = new StringBuilder("");
                Set<String> projectsName = new HashSet<>();
                fSpec.getPredNodes( fSpecNode ).forEach( fSpecNodePred -> {
                    projectsName.add( fSpec.getEdges( fSpecNodePred, fSpecNode ).iterator().next().getTitle() );
                } );
                projectsName.forEach( s -> className.append( className.length() == 0 ? s : ("_" + s) ) );
/*                if(!className.toString().contains("ambari-server-2.4.1.0.22.jar"))
                    return;*/
                CodeGenerator codeGenerator = new CodeGenerator(classHierarchyUtil);
                try {
                    System.out.println( codeGenerator.generateCode( className.toString() , fSpec, (FSpecEndNode) fSpecNode) );
                } catch (CodeGenerationException e) {
                    e.printStackTrace();
                }
            }
        } );

    }
}

