package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecAPIInstantiationNode;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecNode;

import java.util.List;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecApiInstantiationNodeCodeGenerator extends FSpecNodeCodeGenerator<FSpecAPIInstantiationNode> {
    @Override
    // Generates the code related to an API instantiation (e.g. LoginContext loginContext = new LoginContext( "", subject, callbackHandler )
    public String generateCode(FSpec fSpec, FSpecAPIInstantiationNode fSpecAPIInstantiationNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) {
        StringBuilder code = new StringBuilder();

        // Creating a variable name for the to be instantiating API (e.g. loginContext for an instantiation of LoginContext)
        if( definesSymbol( fSpecAPIInstantiationNode ) ) {
            symbolTable.createSymbol(fSpecAPIInstantiationNode);
        }

        // In the case that the API is an abstract class or an interface, or if this is a private/protected constructor,
        // then we do not instantiate it in our code. Instead,
        // we add it as an argument passed to the generated code with variable name as what we just created.
        if( fSpecAPIInstantiationNode.isAbstractOrInterface() || !fSpecAPIInstantiationNode.isPublicConstructor() )
            return code.toString();

        // Generating the code corresponding to variable name creation (e.g. LoginContext loginContext = )
        if( definesSymbol( fSpecAPIInstantiationNode ) ) {
            code.append ( fSpecAPIInstantiationNode.getSimpleClassName() + " " + symbolTable.getSymbol( fSpecAPIInstantiationNode ) + " = " );
        }


        // Generating new keyword alongside with the class of the API (e.g. new LoginContext( )
        code.append( "new " + fSpecAPIInstantiationNode.getSimpleClassName() + "(" );

        // Finding the object reference for this API call and generating the corresponding code (e.g. loginContext.( )
        List<String> argumentTypes = fSpecAPIInstantiationNode.getArgumentTypes();
        Map<String, FSpecNode> argumentsMap = createArgumentsMap(argumentTypes, fSpec.getPredNodes( fSpecAPIInstantiationNode ), classHierarchyUtil);
        for ( int i = 0; i < argumentTypes.size(); i++ ) {
            String argumentType = argumentTypes.get(i);
            FSpecNode correspondingFSpecNode = argumentsMap.get(argumentType);
            String variable = null;
            if( correspondingFSpecNode != null )
                variable = symbolTable.getSymbol( correspondingFSpecNode );
            else {
                variable = createArgumentVariable(argumentType, symbolTable);
            }

            code.append((i > 0 ? ", " : "") +  variable );
        }
        code.append( ")" );

        // Adding the comment to the generated code in the case that this API is an abstract class or an interface and needs to be implemented by the user
        if( fSpecAPIInstantiationNode.isAbstractOrInterface() )
            code.append( " /*This class needs to be implemented/extended by the programmer*/" );
        return  code.toString();


    }
}
