package edu.rit.se.design.fspec2code;

import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import edu.rit.se.design.specminer.analysis.WalaUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class ClassHierarchyUtil {
    String frameworkJarPath;
    String exclusionFilePath;
    IClassHierarchy iClassHierarchy;

    public ClassHierarchyUtil(String frameworkJarPath, String exclusionFilePath) throws ClassHierarchyException, IOException {
        this.frameworkJarPath = frameworkJarPath;
        this.exclusionFilePath = exclusionFilePath;

        this.iClassHierarchy = createClassHierarchyForFramework( frameworkJarPath, exclusionFilePath);
    }

    public boolean isSubclassOf( String subClassTypeName, String superClassTypeName ){
        ClassLoaderReference primordialClassLoaderReference = iClassHierarchy.getLoader( ClassLoaderReference.Primordial ).getReference();
        TypeReference subClassTypeReference = TypeReference.find( primordialClassLoaderReference, subClassTypeName );
        TypeReference superClassTypeReference = TypeReference.find( primordialClassLoaderReference, superClassTypeName );

        if( subClassTypeReference == null || superClassTypeReference == null )
            return false;

        IClass subClass = iClassHierarchy.lookupClass( subClassTypeReference );
        IClass superClass = iClassHierarchy.lookupClass( superClassTypeReference );

        return getSuperClasses( subClass ).contains( superClass );
    }

    protected Set<IClass> getSuperClasses(IClass c ){
        return WalaUtils.getSuperClasses(c, true);
    }

    public boolean isSubclassOf(IClass subClass, IClass superClass){
        return iClassHierarchy.isSubclassOf( subClass, superClass );
    }

    IClassHierarchy createClassHierarchyForFramework(String frameworkJarPath, String exclusionFilePath) throws ClassHierarchyException, IOException {
        // build the class hierarchy
        System.out.print("\tBuilding class hierarchy");
        AnalysisScope scope = createAnalysisScope(frameworkJarPath, exclusionFilePath);
        IClassHierarchy cha =
                ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));

        System.out.println(" -> " + cha.getNumberOfClasses() + " classes were found!");
        return cha;
    }

    AnalysisScope createAnalysisScope(String frameworkJarPath, String exclusionFilePath ) throws IOException {
        File exclusionFile = new File( exclusionFilePath );
        AnalysisScope analysisScope = AnalysisScopeReader.makePrimordialScope(exclusionFile);
        analysisScope.addToScope( ClassLoaderReference.Primordial, new JarFile( frameworkJarPath ) );
        return analysisScope;

    }
}
