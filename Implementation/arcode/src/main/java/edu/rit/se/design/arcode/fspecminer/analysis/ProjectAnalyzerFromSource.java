package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.analysis.reflection.GetMethodContextInterpreter;
import com.ibm.wala.analysis.reflection.GetMethodContextSelector;
import com.ibm.wala.cast.ir.ssa.AstIRFactory;
import com.ibm.wala.cast.java.ipa.callgraph.AstJavaZeroOneContainerCFABuilder;
import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.LambdaMethodTargetSelector;
import com.ibm.wala.types.ClassLoaderReference;
import edu.rit.se.design.arcode.fspecminer.analysis.dependencyresolver.ProjectDependencyResolverFactory;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkUtilityNotFoundException;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkUtilsFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ProjectAnalyzerFromSource extends ProjectAnalyzer {

    static String DEPENDENCIES_FILE_NAME = "dependencies.spm";

    public ProjectAnalyzerFromSource(String sourceFolder, String exclusionFile, String framework) {
        super(sourceFolder, exclusionFile, framework);
    }

    @Override
    protected AnalysisCache createAnalysisCache() {
        return new AnalysisCacheImpl(AstIRFactory.makeDefaultFactory());
    }

    @Override
    protected AnalysisOptions createAnalysisOptions(AnalysisScope analysisScope, IClassHierarchy classHierarchy, Iterable<Entrypoint> distinctEntryPoints) {
        AnalysisOptions options = new AnalysisOptions();
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
        options.setEntrypoints( distinctEntryPoints );
        options.setAnalysisScope( analysisScope );
        options.setSelector(
                new LambdaMethodTargetSelector(new ClassHierarchyMethodTargetSelector(classHierarchy)));
//        options.setSelector(new ClassHierarchyMethodTargetSelector( classHierarchy ));
        options.setSelector(new ClassHierarchyClassTargetSelector(classHierarchy));
//        Util.addDefaultSelectors(options, classHierarchy);
//        Util.addDefaultBypassLogic(options, analysisScope, Util.class.getClassLoader(), classHierarchy);
        return options;
    }

    @Override
    protected CallGraphBuilder createMainCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions options, AnalysisCache cache,
                                                          AnalysisScope analysisScope) {
//        options = CallGraphTestUtil.makeAnalysisOptions(analysisScope, (Iterable<Entrypoint>) options.getEntrypoints());

        ContextSelector appContextSelector =
                new GetMethodContextSelector(false);
//                null;

        SSAContextInterpreter appContextInterpreter =
                new GetMethodContextInterpreter();
//                null;
        return
//                new AstJavaCFABuilder( classHierarchy, options, cache );// JavaZeroXCFABuilder()
//         new AstJavaZeroOneContainerCFABuilder(classHierarchy, options, cache, null, null);
                new AstJavaZeroOneContainerCFABuilder(classHierarchy, options, cache, appContextSelector, appContextInterpreter);

    }

    @Override
    protected CallGraph createCallGraphForEntrypointExtraction(IClassHierarchy classHierarchy,
                                                               AnalysisScope analysisScope, IAnalysisCacheView cache) throws CallGraphBuilderCancelException {
        List<Entrypoint> frameworkBasedEntrypoints = StreamSupport.stream(
                getAllPossibleEntrypoints( analysisScope, classHierarchy ).spliterator(), false ).filter(
                entrypoint -> {
                    try {
                        return FrameworkUtilsFactory.getFrameworkUtils( framework ).isFrameworkEntrypoint( entrypoint );
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
        options.setAnalysisScope( analysisScope );
        options.setSelector(new ClassHierarchyMethodTargetSelector( classHierarchy ));

        Util.addDefaultSelectors(options, classHierarchy);
        Util.addDefaultBypassLogic(options, analysisScope, Util.class.getClassLoader(), classHierarchy);

        SSAContextInterpreter contextInterpreter =
                null;
//                new GetMethodContextInterpreter();
//        new AstContextInsensitiveSSAContextInterpreter(options, cache);
        ContextSelector contextSelector =
                null;
//                new GetMethodContextSelector(true);



        com.ibm.wala.ipa.callgraph.CallGraphBuilder temporaryCGBuilder =
//                new AstJavaZeroXCFABuilder( classHierarchy, options, cache, null, null,  0 );
//                new AstJavaCFABuilder(classHierarchy, options, cache);
                new AstJavaZeroOneContainerCFABuilder(classHierarchy, options, cache, contextSelector, contextInterpreter);
//                new AstJavaZeroOneContainerCFABuilder( classHierarchy, options, cache, null, null);

        CallGraph temporaryCG = temporaryCGBuilder.makeCallGraph(options, null);
        return temporaryCG;
    }

/*    @Override
    protected Iterable<EntrypointInfo> getAllPossibleEntrypointInfos(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
        SourceEntrypointInfoFinder sourceEntrypointInfoFinder = new SourceEntrypointInfoFinder();
        Set<Entrypoint> allEntrypoints = new AllApplicationEntrypoints(analysisScope, classHierarchy);
        return sourceEntrypointInfoFinder.findEntrypointInfos( analysisScope, classHierarchy );
    }*/

    @Override
    protected Iterable<Entrypoint> getAllPossibleEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) {
        return findEntrypoints( analysisScope, classHierarchy );
    }

    public List<Entrypoint> findEntrypoints(AnalysisScope scope, final IClassHierarchy cha) {

        List<Entrypoint> result = new ArrayList<>();

        if (cha == null) {
            throw new IllegalArgumentException("cha is null");
        }

        for (IClass klass : cha) {
            if( !isSourceClass( klass ) || klass.isInterface() )
                continue;
            for (IMethod method : klass.getDeclaredMethods()) {
                if (!method.isAbstract()) {
                    result.add(new SubtypesEntrypoint( method, cha ));
                }
            }
        }
        return result;
    }

    /** @return true iff klass is loaded by the source loader. */
    private static boolean isSourceClass(/*AnalysisScope scope,*/ IClass klass) {
        return klass.getClassLoader().getName().toString().equals( "Source" );
    }

    @Override
    protected AnalysisScope createAnalysisScope() throws IOException {
        AnalysisScope analysisScope = new JavaSourceAnalysisScope();
        List<String> primordialJarPaths = getPrimordialJarFilePaths();
        try {
            primordialJarPaths.addAll( getAllDependencies( getPathToProgram() ) );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

//        String jdkLibDirPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home";
//        primordialJarPaths.addAll( FileUtils.getAllJarFilesInDirectory( jdkLibDirPath ) );
//
//        String mavenLibDirPath = "/Users/ali/.m2/repository";
//        primordialJarPaths.addAll( FileUtils.getAllJarFilesInDirectory( mavenLibDirPath ) );

        // add primordial libraries to scope
        for (String primordialJarPath : new HashSet<>(primordialJarPaths))
            analysisScope.addToScope( ClassLoaderReference.Primordial, new JarFile( primordialJarPath ) );

        // add source folders (if any) libraries to scope
        analysisScope.addToScope(JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(getPathToProgram())));
        return analysisScope;
    }
    List<String> getAllDependencies( String projectFolder ) throws Throwable {
        Path dependenciesFilePath = Paths.get( projectFolder + File.separator + DEPENDENCIES_FILE_NAME );

        if( Files.exists( dependenciesFilePath ) )
            return Files.readAllLines( dependenciesFilePath );

        Files.createFile( dependenciesFilePath );

        List<String> dependencies = ProjectDependencyResolverFactory.getProjectDependencyResolver( projectFolder ).findDependencies( projectFolder );

        Files.write( dependenciesFilePath, dependencies, StandardOpenOption.WRITE);
        return dependencies;

    }

}
