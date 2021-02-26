package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.CodeGenerationException;
import edu.rit.se.design.arcode.fspec2code.graam.GRAAMCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.subfspec.SubFSpecCodeGenerator;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecEndNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CodeGeneratorUtil {
    public static StringBuilder SubFSpec2Code(SubFSpec subFSpec, String classComment, String methodName, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException {
        SubFSpecCodeGenerator codeGenerator = new SubFSpecCodeGenerator( classHierarchyUtil );
        FSpecEndNode fSpecEndNode = (FSpecEndNode) StreamSupport.stream( subFSpec.spliterator(), false).
                filter( fSpecNode -> fSpecNode instanceof FSpecEndNode ).collect( Collectors.toList() ).get(0);
        return codeGenerator.generateCode( classComment, methodName, subFSpec, fSpecEndNode );
    }

    public static StringBuilder GRAAM2Code(GRAAM graam, String classComment, String methodName, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException {
        GRAAMCodeGenerator codeGenerator = new GRAAMCodeGenerator( classHierarchyUtil );
        DirectedGraphNode graamEndNode = graam.getEndNode();
        return codeGenerator.generateCode( classComment, methodName, graam, graamEndNode );
    }
}
