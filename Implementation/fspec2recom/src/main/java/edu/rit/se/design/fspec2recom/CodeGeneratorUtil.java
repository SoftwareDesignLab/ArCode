package edu.rit.se.design.fspec2recom;

import edu.rit.se.design.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.fspec2code.CodeGenerationException;
import edu.rit.se.design.fspec2code.CodeGenerator;
import edu.rit.se.design.specminer.fspec.FSpecEndNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class CodeGeneratorUtil {
    public static StringBuilder SubFSpec2Code(SubFSpec subFSpec, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException {
        CodeGenerator codeGenerator = new CodeGenerator( classHierarchyUtil );
        FSpecEndNode fSpecEndNode = (FSpecEndNode) StreamSupport.stream( subFSpec.spliterator(), false).
                filter( fSpecNode -> fSpecNode instanceof FSpecEndNode ).collect( Collectors.toList() ).get(0);
        return codeGenerator.generateCode( "Automatically generated code", subFSpec, fSpecEndNode );
    }
}
