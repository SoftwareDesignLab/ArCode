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

package edu.rit.se.design.arcode.fspec2code;

import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import edu.rit.se.design.arcode.fspecminer.analysis.WalaUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

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
