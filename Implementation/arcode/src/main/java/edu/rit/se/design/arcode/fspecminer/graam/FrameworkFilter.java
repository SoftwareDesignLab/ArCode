package edu.rit.se.design.arcode.fspecminer.graam;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import edu.rit.se.design.arcode.fspecminer.analysis.WalaUtils;

import java.util.function.Predicate;

/**
 * It is used to filter nodes which are not related to the framework.
 *
 * @author Ali Shokri <as8308@rit.edu>
 */
public class FrameworkFilter implements Predicate<Statement> {
    FrameworkUtils frameworkUtils;
    public FrameworkFilter(String framework) throws Exception {
        frameworkUtils = FrameworkUtilsFactory.getFrameworkUtils( framework );
    }

    /**
     * Verifies whether the statement is framework-related or not
     *
     * @param s statement to be analyzed
     * @return true if framework-related; false otherwise
     */
    @Override
    public boolean test(Statement s) {
        try {
            if (!s.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) &&
                    !s.getNode().getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Source")
            ) {
                return false;
            }

            IClass c = WalaUtils.getTargetClass(s);


            //TODO: if c inherits from the framework and s is a method call, then the method should
            // be inspected to be sure that it is from or is overriding a method from the framework.


            return c != null && (frameworkUtils.isFromFramework(c) || frameworkUtils.inheritsFromFramework(c));
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
        return false;
    }

    public boolean test(CGNode cgNode, SSAInstruction ssaInstruction){
        NormalStatement normalStatement = new NormalStatement( cgNode, ssaInstruction.iIndex() );
        return test( normalStatement );
    }

}
