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

package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkUtilityNotFoundException;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkUtilsFactory;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ProjectAnalyzerFromJar extends ProjectAnalyzer {
    public ProjectAnalyzerFromJar(String pathToProgramJar, String exclusionFile, String framework) {
        super(pathToProgramJar, exclusionFile, framework);
    }

    @Override
    protected AnalysisCache createAnalysisCache() {
        return new AnalysisCacheImpl();
        //        IAnalysisCacheView cache = new AnalysisCacheImpl( (new ShrikeIRFactory()). );
    }

    @Override
    protected AnalysisOptions createAnalysisOptions(AnalysisScope analysisScope, IClassHierarchy classHierarchy, Iterable<Entrypoint> distinctEntryPoints) {
        AnalysisOptions options = new AnalysisOptions();
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
        options.setEntrypoints(distinctEntryPoints);
        options.setAnalysisScope(analysisScope);
        // TODO: Check the following items again
//        options.setSelector(new ClassHierarchyMethodTargetSelector( classHierarchy ));
//        Util.addDefaultSelectors(options, classHierarchy);
//        Util.addDefaultBypassLogic(options, analysisScope, Util.class.getClassLoader(), classHierarchy);
        return options;
    }

    @Override
    protected CallGraphBuilder createMainCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions options, AnalysisCache cache,
                                                          AnalysisScope analysisScope) {
        return Util.makeNCFABuilder(1, options, cache, classHierarchy, analysisScope);
    }

    @Override
    protected CallGraph createCallGraphForEntrypointExtraction(IClassHierarchy classHierarchy, AnalysisScope analysisScope, IAnalysisCacheView cache) throws CallGraphBuilderCancelException, IOException, FrameworkUtilityNotFoundException {
        List<Entrypoint> frameworkBasedEntrypoints = StreamSupport.stream(
                getAllPossibleEntrypoints(analysisScope, classHierarchy).spliterator(), false).filter(
                entrypoint -> {
                    try {
                        return FrameworkUtilsFactory.getFrameworkUtils(framework).isFrameworkEntrypoint(entrypoint);
                    } catch (FrameworkUtilityNotFoundException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
        ).collect(Collectors.toList());


        // We are using zeroCFA just to find possible entry points. Later, we use NCFA-1 builder for the real call graph
        // Construction.
        AnalysisOptions options = new AnalysisOptions();
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        options.setEntrypoints(frameworkBasedEntrypoints);
        options.setAnalysisScope(analysisScope);
        options.setSelector(new ClassHierarchyMethodTargetSelector(classHierarchy));

        Util.addDefaultSelectors(options, classHierarchy);
        Util.addDefaultBypassLogic(options, analysisScope, Util.class.getClassLoader(), classHierarchy);


        com.ibm.wala.ipa.callgraph.CallGraphBuilder temporaryCGBuilder =
                Util.makeZeroCFABuilder(Language.JAVA, options, cache, classHierarchy, analysisScope);
//        ZeroOneContainerCFABuilderFactory factory = new ZeroOneContainerCFABuilderFactory();
//        CallGraph temporaryCG = factory.make( options, cache, classHierarchy, analysisScope ).makeCallGraph( options, null );
        CallGraph temporaryCG = temporaryCGBuilder.makeCallGraph(options, null);
        return temporaryCG;

    }



/*    @Override
    protected Iterable<EntrypointInfo> getAllPossibleEntrypointInfos(AnalysisScope analysisScope, IClassHierarchy classHierarchy) throws FrameworkUtilityNotFoundException, IOException {
        JarEntrypointInfoFinder jarEntrypointInfoFinder = new JarEntrypointInfoFinder();
        return jarEntrypointInfoFinder.findEntrypointInfos(classHierarchy, getPathToProgram());
    }*/

    @Override
    protected Iterable<Entrypoint> getAllPossibleEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) throws FrameworkUtilityNotFoundException, IOException {
        return findEntrypoints(classHierarchy, getPathToProgram());
    }


    protected List<Entrypoint> findEntrypoints(IClassHierarchy cha, String jarFilePath) throws IOException {
        if (cha == null) {
            throw new IllegalArgumentException("IClassHierarchy cha parameter is null");
        }

        final HashSet<Entrypoint> result = HashSetFactory.make();

        // get methods that include a login context instantiation
        JarFile jarFile = new JarFile(jarFilePath);
        Enumeration<JarEntry> entries = jarFile.entries();
        List<JarEntry> entryList = new ArrayList<>();
        while (entries.hasMoreElements())
            entryList.add(entries.nextElement());

        for (JarEntry entry : entryList) {

            String entryName = entry.getName();
            if (entryName.endsWith(".class")) {
//                classCounter++;
                InputStream in = jarFile.getInputStream(entry);
                ClassReader reader = new ClassReader(in);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);
                classNode.methods.forEach((MethodNode method) -> {
//                    if( isAnEntrypoint( cha, classNode, method ) )
                    try {
                        IClass applicationIClass = WalaUtils.getIClassForApplicationClassNode(cha, classNode);
                        if (applicationIClass == null)
                            return;
                        IMethod applicationIMethod = WalaUtils.getIMethod(applicationIClass, method);
                        if (applicationIMethod == null)
                            // TODO: sometimes lambda methods are compiled with a name but are loaded in the ClassHierarchy with different names (e.g. labda$0 and lambda$0$servide)
                            return;
                        Entrypoint entrypoint = WalaUtils.convertToEntrypoint(cha, applicationIMethod);
                        if (FrameworkUtilsFactory.getFrameworkUtils(getFramework()).isFrameworkEntrypoint(entrypoint))
                            result.add(entrypoint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

/*
                classNode.methods.forEach((MethodNode method) -> {
                    if (WalaUtils.isMainMethod(method)) {
                        try {
                            result.add(WalaUtils.convertToEntrypoint(cha, classNode, method));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ListIterator<AbstractInsnNode> it = method.instructions.iterator();
                        while (it.hasNext()) {
                            AbstractInsnNode instruction = it.next();
                            if (instruction.getOpcode() == Opcodes.NEW) {
                                TypeInsnNode typeIns = (TypeInsnNode) instruction;
                                if (typeIns.desc.equals("javax/security/auth/login/LoginContext")) {
                                    try {
                                        result.add(WalaUtils.convertToEntrypoint(cha, classNode, method));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }



                });*/
            }
        }
        return new ArrayList<>(result);
    }

    @Override
    protected AnalysisScope createAnalysisScope() throws IOException {
        File exclusionFile = new File(getExclusionFile());
        AnalysisScope analysisScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(getPathToProgram(), exclusionFile);

        AnalysisScope primordialScope = AnalysisScopeReader.makePrimordialScope(exclusionFile);

        //Commented by Ali: It seems that WALA is already adding WalaProperties.getJ2SEJarFiles() as Primordial. So we do not need to do that.
        List<String> primordialJarPaths = getPrimordialJarFilePaths();

//        String jdkLibDirPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home";
//        primordialJarPaths.addAll( FileUtils.getAllJarFilesInDirectory( jdkLibDirPath ) );
//
//        String mavenLibDirPath = "/Users/ali/.m2/repository";
//        primordialJarPaths.addAll( FileUtils.getAllJarFilesInDirectory( mavenLibDirPath ) );

        // add primordial libraries to scope
        for (String primordialJarPath : new HashSet<>(primordialJarPaths))
            primordialScope.addToScope( ClassLoaderReference.Primordial, new JarFile( primordialJarPath ) );

        // TODO: This is an ad hoc fix for ArCodePlugin. It should be fixed in the future

        Path temp = Files.createTempFile("resource-", ".jar");
        Files.copy(this.getClass().getClassLoader().getResourceAsStream("javaee-api-8.0.jar"), temp, StandardCopyOption.REPLACE_EXISTING);
        primordialScope.addToScope( ClassLoaderReference.Primordial, new JarFile( temp.toFile() ) );

        Path temp2 = Files.createTempFile("resource-", ".jar");
        Files.copy(this.getClass().getClassLoader().getResourceAsStream("java-rt.jar"), temp2, StandardCopyOption.REPLACE_EXISTING);
        primordialScope.addToScope( ClassLoaderReference.Primordial, new JarFile( temp2.toFile() ) );

        analysisScope.addToScope( primordialScope );


        // add dependency folder (if exists) to scope
//        AnalysisScope dependenciesScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(projectDir.getParent() + "/dependencies" , exFile);
//        analysisScope.addToScope( dependenciesScope );
        return analysisScope;
    }
}
