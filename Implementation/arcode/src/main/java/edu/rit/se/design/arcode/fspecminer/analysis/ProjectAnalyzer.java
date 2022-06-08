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

import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkUtilityNotFoundException;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class ProjectAnalyzer {
    String pathToProgram;
    String exclusionFile;
    String framework;

    public ProjectAnalyzer(String pathToProgram, String exclusionFile, String framework){
        setPathToProgram( pathToProgram );
        setExclusionFile( exclusionFile );
        setFramework( framework );
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getPathToProgram() {
        return pathToProgram;
    }

    public void setPathToProgram(String pathToProgram) {
        this.pathToProgram = pathToProgram;
    }

    public String getExclusionFile() {
        return exclusionFile;
    }

    public void setExclusionFile(String exclusionFile) {
        this.exclusionFile = exclusionFile;
    }

    protected AnalysisScope prepareAnalysisScope() throws IOException {
        AnalysisScope scope = createAnalysisScope();
        scope.setExclusions( new SetOfExclusionClasses(getExclusionFile() ) );
//        scope.setExclusions( new SetOfInclusionClasses(getExclusionFile() ) );
        return scope;
    }

    protected IClassHierarchy createClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        // build the class hierarchy
        CommonConstants.LOGGER.log( Level.FINE, "\tBuilding class hierarchy" );

        IClassHierarchy cha =
                ClassHierarchyFactory.make(scope, new ECJClassLoaderFactory(scope.getExclusions()));

        CommonConstants.LOGGER.log( Level.FINE, " -> " + cha.getNumberOfClasses() + " classes were found!" );
        return cha;
    }



    protected Iterable<Entrypoint> findDistinctEntryPoints(IClassHierarchy classHierarchy, AnalysisScope analysisScope, IAnalysisCacheView cache) throws IOException, FrameworkUtilityNotFoundException, CallGraphBuilderCancelException {
        CommonConstants.LOGGER.log( Level.FINE, "\tFinding entry points");
        CallGraph temporaryCG = createCallGraphForEntrypointExtraction(classHierarchy, analysisScope, cache);
        Set<Entrypoint> distinctEntryPoints = getDistinctEntryPoints(temporaryCG );
        CommonConstants.LOGGER.log( Level.FINE, " -> " + distinctEntryPoints.size() + " distinct entry point(s) were found!");
        return distinctEntryPoints;
    }

    protected abstract AnalysisCache createAnalysisCache();
    protected abstract AnalysisOptions createAnalysisOptions(AnalysisScope analysisScope, IClassHierarchy classHierarchy,
                                                             Iterable<Entrypoint> distinctEntryPoints);

    protected abstract CallGraphBuilder createMainCallGraphBuilder(IClassHierarchy classHierarchy, AnalysisOptions options,
                                                                   AnalysisCache cache, AnalysisScope analysisScope);

    public ProjectAnalysis analyzeProject() throws Exception {
        AnalysisScope analysisScope = prepareAnalysisScope();
        IClassHierarchy classHierarchy = createClassHierarchy(analysisScope);
        AnalysisCache cache = createAnalysisCache();
        Iterable<Entrypoint> distinctEntryPoints = findDistinctEntryPoints(classHierarchy, analysisScope, cache);
        AnalysisOptions options = createAnalysisOptions( analysisScope, classHierarchy, distinctEntryPoints);


        CommonConstants.LOGGER.log( Level.FINE, "\tBuilding the call graph");

        // Creating the final CallGraph which we use in our analysis
        CallGraphBuilder cgBuilder = createMainCallGraphBuilder( classHierarchy, options, cache, analysisScope );
        CallGraph cg = cgBuilder.makeCallGraph(options, null);
        CallGraphStats.CGStats callGraphStats = CallGraphStats.getCGStats(cg);

        CommonConstants.LOGGER.log( Level.FINE, " -> A call graph with " + callGraphStats.getNNodes() + " nodes, " + callGraphStats.getNEdges() + " edges "
                + "was created!");
//        System.out.println("\t\t" + CallGraphStats.getStats(cg));

        PointerAnalysis<InstanceKey> pa = (PointerAnalysis<InstanceKey>) cgBuilder.getPointerAnalysis();
//        StringBuilder paDot = DotUtil.dotOutput(pa.getHeapGraph(), null, "");
//        System.out.println( paDot.toString() );

        return new ProjectAnalysis( new ProjectInfo(getPathToProgram()), cg, pa );
    }
    protected List<String> getPrimordialJarFilePaths(){
        List<String> primordialJarFilePaths = new ArrayList<>( Arrays.asList( WalaProperties.getJ2SEJarFiles() ) ) ;
//        primordialJarFilePaths.add( "/Users/as8308/Desktop/Ali/RIT/My Drive/Research/Projects/NewSpecMiner/Implementation/specminer/config/javaee-api-8.0.jar" );
//        primordialJarFilePaths.add( "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/config/javaee-api-8.0.1.jar" );
//        primordialJarFilePaths.add( "/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home/jre/lib/rt.jar" );

        // Despite Wala 1.4.3, Wala 1.5.4 version does not add all the jar files in the jre/lib directory as primordial jars.
        // So here we add them manually
        /*primordialJarFilePaths.addAll(
                Arrays.asList(WalaProperties.getJarsInDirectory( "/Library/Java/JavaVirtualMachines/jdk1.8.0_251.jdk/Contents/Home/jre/lib" )) );*/

        return primordialJarFilePaths;
    }

    protected abstract CallGraph createCallGraphForEntrypointExtraction(IClassHierarchy classHierarchy, AnalysisScope analysisScope, IAnalysisCacheView cache) throws CallGraphBuilderCancelException, IOException, FrameworkUtilityNotFoundException;

    protected abstract Iterable<Entrypoint> getAllPossibleEntrypoints(AnalysisScope analysisScope, IClassHierarchy classHierarchy) throws FrameworkUtilityNotFoundException, IOException;


    protected abstract AnalysisScope createAnalysisScope() throws IOException;

    // Refines entry points. We remove entry points that are reachable from other entry points.
    protected Set<Entrypoint> getDistinctEntryPoints(CallGraph cg){
        Set<CGNode> distinctEntryPointCGNodes = HashSetFactory.make();
        if( cg.getEntrypointNodes().size() < 2 )
            distinctEntryPointCGNodes.addAll(cg.getEntrypointNodes());
        else {
            List<CGNode> entrypointCGNodeList = new ArrayList<>();

            cg.getEntrypointNodes().forEach(entrypointNode1 -> entrypointCGNodeList.add(entrypointNode1));

            for (int i = 0; i < entrypointCGNodeList.size(); i++) {
                // if it is a sub-graph of at least one other graph, then it is not a distinct entry point. So, investigate the next node (i)
                boolean isSubGraph = false;
                for (int j = 0; j < entrypointCGNodeList.size(); j++) {
                    if (i == j)
                        continue;
                    if (isSubGraphOf(cg, entrypointCGNodeList.get(i), entrypointCGNodeList.get(j))) {
                        isSubGraph = true;
//                    LoggerUtils.getLogger(ProjectAnalysisFactory.class).finest( "getDistinctEntryPoints -> node: " + entrypointCGNodeList.get(i) + "\n\t is accessible through: " +entrypointCGNodeList.get(j) );

                        break;
                    }
                }
                if (!isSubGraph)
                    distinctEntryPointCGNodes.add(entrypointCGNodeList.get(i));
            }
        }

        Set<Entrypoint> result = HashSetFactory.make();
        distinctEntryPointCGNodes.forEach( cgNode -> {
            SubtypesEntrypoint subtypesEntrypoint = new SubtypesEntrypoint(cgNode.getMethod(), cgNode.getClassHierarchy());
//            if( subtypesEntrypoint == null )
//                LoggerUtils.getLogger( ProjectAnalysisFactory.class ).severe( "No entry point could be generated for " + cgNode );
            result.add( subtypesEntrypoint );
        } );

//        result.forEach( entrypoint -> System.out.println( "\t\t" + entrypoint.toString() ) );
        return result;
    }

    protected boolean isSubGraphOf(CallGraph cg, CGNode subGraphEntryNode, CGNode superGraphEntryNode ){
        Set<CGNode> visitedCGNodes = HashSetFactory.make();
        boolean foundNode = findCGNode( cg, superGraphEntryNode, subGraphEntryNode, visitedCGNodes );
        if( foundNode )
            return true;
        return false;
    }

    protected boolean findCGNode(CallGraph cg, CGNode currentNode, CGNode toBeFoundNode, Set<CGNode> visitedNodes) {
/*
        if( currentNode.toString().equals( "Node: < Application, LJAASTestClass, createLoginContext(Ljava/lang/String;Ljavax/security/auth/callback/CallbackHandler;)Ljavax/security/auth/login/LoginContext; > Context: CallStringContext: [ com.ibm.wala.FakeRootClass.fakeRootMethod()V@25 ]" ) )
            System.out.print("");
*/
        if( visitedNodes.contains( currentNode ) )
            return false;
        visitedNodes.add( currentNode );
        if ( areTheSame( currentNode, toBeFoundNode ) )
            return true;
        Iterator<CGNode> currentNodeSuccItr = cg.getSuccNodes( currentNode );
        while ( currentNodeSuccItr.hasNext() ){
            if( findCGNode( cg, currentNodeSuccItr.next(), toBeFoundNode, visitedNodes ) )
                return true;
        }

        return false;
    }



    protected boolean areTheSame( CGNode cgNode1, CGNode cgNode2 ){
        return cgNode1.equals( cgNode2 );
    }

}
