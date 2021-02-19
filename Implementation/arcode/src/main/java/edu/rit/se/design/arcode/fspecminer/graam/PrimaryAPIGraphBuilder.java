package edu.rit.se.design.arcode.fspecminer.graam;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.graph.Graph;
import edu.rit.se.design.arcode.fspecminer.analysis.*;
import edu.rit.se.design.arcode.fspecminer.ifd.IFD;
import edu.rit.se.design.arcode.fspecminer.ifd.IFDEdgeType;
import edu.rit.se.design.arcode.fspecminer.ifd.MethodRepresentation;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class PrimaryAPIGraphBuilder {
/*    SDG<InstanceKey> dataOnlySdg;
    SDG<InstanceKey> controlOnlySdg;*/
/*    CallGraph callGraph;
    PointerAnalysis pointerAnalysis;
    IFD ifd;*/
/*
    public PrimaryAPIGraphBuilder(CallGraph callGraph, PointerAnalysis pointerAnalysis, IFD ifd) {
        this.callGraph = callGraph;
        this.pointerAnalysis = pointerAnalysis;
        this.ifd = ifd;
    }*/
    static final String SERIALIZED_PRIMARY_API_GRAPH_EXTENSION = ".psrz";
    static final String DOT_GRAPH_EXTENSION = ".dot";

    public static List<PrimaryAPIUsageGraph> buildPrimaryAPIUsageGraphs(ProjectAnalysis projectAnalysis, IFD ifd, boolean validatePAUGs) throws Exception {
        List<PrimaryAPIUsageGraph> primaryAPIUsageGraphList = new ArrayList<>();
        CallGraph callGraph = projectAnalysis.getCallGraph();
        PointerAnalysis pointerAnalysis = projectAnalysis.getPointerAnalysis();
        ProjectInfo projectInfo = projectAnalysis.getProjectInfo();
        Set<Statement> relevantStatements = findRelevantStatements(ifd.getFramework(), callGraph);

//        AtomicReference<StringBuilder> controlOnlySdgDot = new AtomicReference<>(new StringBuilder());
//        AtomicReference<StringBuilder> dataOnlySdgDot = new AtomicReference<>(new StringBuilder());

        FrameworkUtils frameworkUtils = FrameworkUtilsFactory.getFrameworkUtils( ifd.getFramework() );

        CommonConstants.LOGGER.log( Level.FINE, "\tCreating PrimaryAPIUsageGraph(s)" );

        callGraph.getEntrypointNodes().forEach(entrypoint -> {
            try {

//                System.out.println("\t\t Creating a sequence-based slice of the call graph");
                CallGraphSequenceBasedSlicer callGraphSequenceBasedSlicer = new CallGraphSequenceBasedSlicer();
                Graph<Statement> sequenceBasedSlicedCallGraph = callGraphSequenceBasedSlicer.sliceCallGraph(callGraph, entrypoint, relevantStatements);

//                System.out.println("\t\t Creating a data-based slice of the call graph");
                CallGraphDataBasedSlicer callGraphDataBasedSlicer = new CallGraphDataBasedSlicer();
                Graph<Statement> dataBasedSlicedCallGraph = callGraphDataBasedSlicer.sliceCallGraph(callGraph, pointerAnalysis, relevantStatements);

                PrimaryAPIUsageGraph primaryAPIUsageGraph = createPrimaryAPIUsageGraph( projectInfo, sequenceBasedSlicedCallGraph,
                        dataBasedSlicedCallGraph, relevantStatements, frameworkUtils);


                if (primaryAPIUsageGraph.getNumberOfNodes() == 0)
                    return;

//                System.out.println("PrimaryAPIUsageGraph after controlOnlySdg:\n" + controlOnlySdgDot);
//                System.out.println("PrimaryAPIUsageGraph after dataOnlySdg:\n" + dataOnlySdgDot);

                addStartAndEndNodes(primaryAPIUsageGraph);
/*
                System.out.println("PrimaryAPIUsageGraph: \n" + new PrimaryAPIUsageGraphVisualizer( primaryAPIUsageGraph).dotOutput() );


*/

                primaryAPIUsageGraphList.add(primaryAPIUsageGraph);
            }
            catch (Exception e){
                System.out.print("\n\t\tERROR: " + e.getMessage());
            }

        } );

        List<PrimaryAPIUsageGraph> distinctPrimaryAPIUsageGraphs = getDistinctPrimaryAPIUsageGraphs( primaryAPIUsageGraphList );

        List<PrimaryAPIUsageGraph> validatedDistinctPrimaryAPIUsageGraphs = validatePAUGs ?
                StreamSupport.stream(distinctPrimaryAPIUsageGraphs.spliterator(), false).
                        filter( primaryAPIUsageGraph -> isValidPrimaryAPIUsageGraph( primaryAPIUsageGraph ) && !violatesIFD( primaryAPIUsageGraph, ifd ) ).
                        collect(Collectors.toList()) : distinctPrimaryAPIUsageGraphs;

        CommonConstants.LOGGER.log( Level.FINE, " -> " + distinctPrimaryAPIUsageGraphs.size() + " PrimaryAPIUsageGraph(s) was/were created, " + validatedDistinctPrimaryAPIUsageGraphs.size() + " was/were accepted as valid PrimaryAPIUsageGraph(s)!" );

        return validatedDistinctPrimaryAPIUsageGraphs;
    }


    static boolean violatesIFD( PrimaryAPIUsageGraph primaryAPIUsageGraph, IFD ifd ){
        HashMap<DirectedGraphNode, Set<DirectedGraphNode>> ifdNodeDescendentsMap = createPrecedenceMap( ifd, IFDEdgeType.FIELD_BASE_DEPENDENCY);
        HashMap<DirectedGraphNode, Set<DirectedGraphNode>> paugNodeDescendentsMap = createPrecedenceMap( primaryAPIUsageGraph, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY);

        for( DirectedGraphNode paugNode: paugNodeDescendentsMap.keySet() ) {
            if( !(paugNode instanceof FrameworkRelatedNode) )
                continue;
            Set<DirectedGraphNode> paugNodeDescendents = paugNodeDescendentsMap.get( paugNode );
            for (DirectedGraphNode paugNodeDescendent : paugNodeDescendents) {
                if( !(paugNodeDescendent instanceof FrameworkRelatedNode) )
                    continue;

                for( DirectedGraphNode ifdNode: ifdNodeDescendentsMap.keySet() ) {
                    Set<DirectedGraphNode> ifdNodeDescendents = ifdNodeDescendentsMap.get( ifdNode );
                    for( DirectedGraphNode ifdNodeDescendent: ifdNodeDescendents) {
                        if (areIFDNodeAndPAUGNodeTheSame((MethodRepresentation) ifdNode, (FrameworkRelatedNode) paugNodeDescendent) &&
                                areIFDNodeAndPAUGNodeTheSame((MethodRepresentation) ifdNodeDescendent, (FrameworkRelatedNode) paugNode))
                            return true;
                    }
                }
            }
        }

        return false;
    }

    public static List<PrimaryAPIUsageGraph> loadPAGsFromSerializedFolder(String serializedPAGsFolder) throws IOException, ClassNotFoundException {
        List<PrimaryAPIUsageGraph> loadedPAGs = new ArrayList<>();
        List<File> filesInFolder = Files.walk(Paths.get(serializedPAGsFolder))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        File parentFolder = new File(serializedPAGsFolder);

        for (File file : filesInFolder) {
            if (!file.getParentFile().getAbsolutePath().equals(parentFolder.getAbsolutePath()) || !file.getName().endsWith(SERIALIZED_PRIMARY_API_GRAPH_EXTENSION))
                continue;
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            PrimaryAPIUsageGraph retrievedPAG = (PrimaryAPIUsageGraph) objectIn.readObject();
            objectIn.close();
            loadedPAGs.add(retrievedPAG);
        }

        return loadedPAGs;
    }

    public static void saveSerializedPAGs(List<PrimaryAPIUsageGraph> pags, String serializedPAGsFolder) {
        Set<String> createdFiles = new HashSet<>();
        File folder = new File(serializedPAGsFolder);
        if (!folder.exists())
            folder.mkdir();

        pags.forEach(pag -> {
            String fileName = generateUniqueFileName(pag.getTitle(), createdFiles);
            createdFiles.add(fileName);

            String filePath = serializedPAGsFolder + File.separator + fileName + SERIALIZED_PRIMARY_API_GRAPH_EXTENSION;
            FileOutputStream serializedGraspFile = null;
            try {
                serializedGraspFile = new FileOutputStream(filePath);
                ObjectOutputStream objectOut = new ObjectOutputStream(serializedGraspFile);
                objectOut.writeObject(pag);
                objectOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void savePAUGsDotGraph(List<PrimaryAPIUsageGraph> paugs, String paugDotGraphFolder) {
        Set<String> createdFiles = new HashSet<>();
        File folder = new File(paugDotGraphFolder);
        if (!folder.exists())
            folder.mkdir();

        paugs.forEach(paug -> {
            String fileName = generateUniqueFileName(paug.getTitle(), createdFiles);
            createdFiles.add(fileName);

            String filePath = paugDotGraphFolder + File.separator + fileName + DOT_GRAPH_EXTENSION;
            try {
                FileWriter fileWriter = new FileWriter( filePath );
                fileWriter.write( (new PrimaryAPIUsageGraphVisualizer( paug )).dotOutput().toString() );
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }


    static String generateUniqueFileName(String suggestedFileName, Set<String> takenFileNames) {
        String generatedFileName = suggestedFileName;
        int fileNameCounter = 1;
        while (takenFileNames.contains(generatedFileName)) {
            generatedFileName =
                    (fileNameCounter == 1 ?
                            generatedFileName + "_1" :
                            generatedFileName.substring(0, generatedFileName.length() - (String.valueOf(fileNameCounter).length() + 1)) + "_" + fileNameCounter);
            fileNameCounter++;
        }
        return generatedFileName;
    }

    static boolean areIFDNodeAndPAUGNodeTheSame(MethodRepresentation ifdNode, FrameworkRelatedNode paugNode){
        String methodRepresentationToString = ifdNode.getClassNode().name + "." + ifdNode.getMethodNode().name + ifdNode.getMethodNode().desc;
        String graphNodeToString = paugNode.getFrameworkRelatedClass() + "." + paugNode.getFrameworkRelatedMethod();
        return methodRepresentationToString.equals(graphNodeToString);
    }



    static HashMap<DirectedGraphNode, Set<DirectedGraphNode>> createPrecedenceMap(DirectedGraph graph, DirectedGraphEdgeType edgeType){
        HashMap<DirectedGraphNode, Set<DirectedGraphNode>> map = new HashMap<>();

        graph.iterator().forEachRemaining( nodeObj -> {
            graph.getSuccNodes((DirectedGraphNode) nodeObj, edgeType ).forEach( succNodeObj -> {
                addPrecedenceRelationship( (DirectedGraphNode)nodeObj, (DirectedGraphNode)succNodeObj, map );
            } );
        } );

/*
        HashMap<DirectedGraphNode, Set<DirectedGraphNode>> frameworkRelatedNodesMap = new HashMap<>();

        map.keySet().forEach( node -> {
            if( !(node instanceof FrameworkRelatedNode) )
                return;
            map.get( node ).forEach( toNode -> {
                if( !(toNode instanceof FrameworkRelatedNode) )
                    return;
                if( !frameworkRelatedNodesMap.containsKey( node ) )
                    frameworkRelatedNodesMap.put( node, new HashSet<>() );
                frameworkRelatedNodesMap.get( node ).add( toNode );
            } );
        } );
*/

        return map;
    }

    static void addPrecedenceRelationship(DirectedGraphNode fromNode, DirectedGraphNode toNode, HashMap<DirectedGraphNode, Set<DirectedGraphNode>> map) {
        if( !map.containsKey( fromNode ) )
            map.put( fromNode, new HashSet<>() );

        map.get( fromNode ).add( toNode );

        // Establish relationships between fromNode and toNode children (if available in the map)
        if( map.containsKey( toNode ) )
            map.get(toNode).iterator().forEachRemaining( toNodeSucc -> {
                addPrecedenceRelationship( fromNode, toNodeSucc, map );
            } );

        // Establish relationships between fromNode's parents (if available in the map) and toNode
        map.forEach( (n1, n2) -> {
            if( n2.contains( fromNode ) )
                addPrecedenceRelationship( n1, toNode, map );
        } );
    }


    /*
    Checks to make sure that there is no non-static API call without a data-dependency edge to its reference
    FIXME: Currently, it simply checks any data dependencies regardless of being its object reference or its arguments.
     */
    static boolean isValidPrimaryAPIUsageGraph( PrimaryAPIUsageGraph primaryAPIUsageGraph ) {
        List<DirectedGraphNode> apiNodes = StreamSupport.stream( primaryAPIUsageGraph.spliterator(), false ).
                filter( directedGraphNode -> directedGraphNode instanceof FrameworkRelatedNode ).collect(Collectors.toList());

        for (DirectedGraphNode node : apiNodes) {
            FrameworkRelatedNode apiNode = (FrameworkRelatedNode) node;
            if( apiNode.isNormalMethodCall() && !apiNode.isStaticMethod() ){
                if( primaryAPIUsageGraph.getPredNodeCount( apiNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ) == 0 )
//                    throw new PrimaryAPIUsageGraphValidationException("No data dependency could be find for non-static method call " + apiNode);
                    return false;
            }
        }
        return true;
    }

    static PrimaryAPIUsageGraph createPrimaryAPIUsageGraph( ProjectInfo projectInfo, Graph<Statement> sequenceBasedCallGraphSlice, Graph<Statement> dataBasedCallGraphSlice, Set<Statement> relevantStatements, FrameworkUtils frameworkUtils ){
        PrimaryAPIUsageGraph primaryAPIUsageGraph = new PrimaryAPIUsageGraph(projectInfo, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY);
        Map<Statement, DirectedGraphNode> statementDirectedGraphNodeMap = new HashMap<>();
        sequenceBasedCallGraphSlice.iterator().forEachRemaining( statement -> {
            try {
                DirectedGraphNode graphNode = toGraphNode( statement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );
//                if( !primaryAPIUsageGraph.containsNode( graphNode ) )
                primaryAPIUsageGraph.addNode( graphNode );
            } catch (FrameworkUtilityNotFoundException e) {
                e.printStackTrace();
            }
        });

        sequenceBasedCallGraphSlice.iterator().forEachRemaining( fromStatement -> {
            sequenceBasedCallGraphSlice.getSuccNodes( fromStatement ).forEachRemaining( toStatement -> {
                try {
                    DirectedGraphNode fromNode = toGraphNode( fromStatement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );
                    DirectedGraphNode toNode =  toGraphNode( toStatement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );
                    if( !primaryAPIUsageGraph.hasEdge( fromNode, toNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ) )
                        primaryAPIUsageGraph.addEdge( fromNode, toNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
                } catch (FrameworkUtilityNotFoundException e) {
                    e.printStackTrace();
                }
            } );
        } );

/*
        dataBasedCallGraphSlice.iterator().forEachRemaining( statement -> {
            try {
                DirectedGraphNode graphNode = toGraphNode( statement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );

                if( !primaryAPIUsageGraph.containsNode( graphNode ) )
                    primaryAPIUsageGraph.addNode( graphNode );
            } catch (FrameworkUtilityNotFoundException e) {
                e.printStackTrace();
            }
        });
*/
        // Establishes data dependencies between nodes that are introduced in sequence-based slice.
        // If a node is not visited in the sequence slice, then ignores it
        dataBasedCallGraphSlice.iterator().forEachRemaining( fromStatement -> {
            dataBasedCallGraphSlice.getSuccNodes( fromStatement ).forEachRemaining( toStatement -> {
                try {
                    DirectedGraphNode fromNode = toGraphNode( fromStatement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );
                    DirectedGraphNode toNode = toGraphNode( toStatement, relevantStatements, frameworkUtils, statementDirectedGraphNodeMap );
                    if( primaryAPIUsageGraph.containsNode( fromNode ) && primaryAPIUsageGraph.containsNode( toNode ) )
                        if( !primaryAPIUsageGraph.hasEdge( fromNode, toNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ) )
                            primaryAPIUsageGraph.addEdge( fromNode, toNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY );
                } catch (FrameworkUtilityNotFoundException e) {
                    e.printStackTrace();
                }
            } );
        } );

        return primaryAPIUsageGraph;
    }

    static boolean isSubGraphOf( PrimaryAPIUsageGraph subGraph, PrimaryAPIUsageGraph superGraph  ){
        Iterator<DirectedGraphNode> subGraphNodeItr = subGraph.iterator();
        while ( subGraphNodeItr.hasNext() ){
            DirectedGraphNode subGraphNode = subGraphNodeItr.next();
            if( subGraphNode instanceof NonFrameworkRelatedNode  )
                continue;
            if( !superGraph.containsNode( subGraphNode ) )
                return false;

            DirectedGraphNode superGraphNode = subGraphNode;

            // Checking pred SEQ
            List<DirectedGraphNode> subGraphNodePredSeq = new ArrayList<>();
            subGraph.getPredNodes( subGraphNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ).forEach( subGraphNodePredSeq::add );
            subGraphNodePredSeq = subGraphNodePredSeq.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            List<DirectedGraphNode> superGraphNodePredSeq = new ArrayList<>();
            subGraph.getPredNodes( superGraphNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ).forEach( superGraphNodePredSeq::add );
            superGraphNodePredSeq = superGraphNodePredSeq.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            if( !superGraphNodePredSeq.containsAll( subGraphNodePredSeq ) )
                return false;

            // Checking pred DATA
            List<DirectedGraphNode> subGraphNodePredData = new ArrayList<>();
            subGraph.getPredNodes( subGraphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ).forEach( subGraphNodePredData::add );
            subGraphNodePredData = subGraphNodePredData.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            List<DirectedGraphNode> superGraphNodePredData = new ArrayList<>();
            subGraph.getPredNodes( superGraphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ).forEach( superGraphNodePredData::add );
            superGraphNodePredData = superGraphNodePredData.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            if( !superGraphNodePredData.containsAll( subGraphNodePredData ) )
                return false;

            // Checking succ SEQ
            List<DirectedGraphNode> subGraphNodeSuccSeq = new ArrayList<>();
            subGraph.getSuccNodes( subGraphNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ).forEach( subGraphNodeSuccSeq::add );
            subGraphNodeSuccSeq = subGraphNodeSuccSeq.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            List<DirectedGraphNode> superGraphNodeSuccSeq = new ArrayList<>();
            subGraph.getSuccNodes( superGraphNode, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ).forEach( superGraphNodeSuccSeq::add );
            superGraphNodeSuccSeq = superGraphNodeSuccSeq.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            if( !superGraphNodeSuccSeq.containsAll( subGraphNodeSuccSeq ) )
                return false;

            // Checking succ DATA
            List<DirectedGraphNode> subGraphNodeSuccData = new ArrayList<>();
            subGraph.getSuccNodes( subGraphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ).forEach( subGraphNodeSuccData::add );
            subGraphNodeSuccData = subGraphNodeSuccData.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            List<DirectedGraphNode> superGraphNodeSuccData = new ArrayList<>();
            subGraph.getSuccNodes( superGraphNode, PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ).forEach( superGraphNodeSuccData::add );
            superGraphNodeSuccData = superGraphNodeSuccData.stream().filter( graphNode -> (graphNode instanceof FrameworkRelatedNode) ).collect(Collectors.toList());

            if( !superGraphNodeSuccData.containsAll( subGraphNodeSuccData ) )
                return false;
        }
        return true;
    }


    static List<PrimaryAPIUsageGraph> getDistinctPrimaryAPIUsageGraphs( List<PrimaryAPIUsageGraph> primaryAPIUsageGraphs ){
        Map<PrimaryAPIUsageGraph, Set<PrimaryAPIUsageGraph>> subGraphMap = new HashMap<>();
        primaryAPIUsageGraphs.forEach( superGraph -> {
            primaryAPIUsageGraphs.forEach( subGraph -> {
                if( subGraph.equals( superGraph ) )
                    return;
                if( isSubGraphOf( subGraph, superGraph ) && !isSubGraphOf( superGraph, subGraph )) { // To prevent considering graphs that are the same
                    if( !subGraphMap.containsKey( subGraph ) )
                        subGraphMap.put( subGraph, new HashSet<>() );
                    subGraphMap.get(subGraph).add(superGraph);
                }
            } );
        } );
        List<PrimaryAPIUsageGraph> distinctPrimaryAPIUsageGraphs = new ArrayList<>( primaryAPIUsageGraphs );
        distinctPrimaryAPIUsageGraphs.removeAll( subGraphMap.keySet() );
        return distinctPrimaryAPIUsageGraphs;
    }

    static List<DirectedGraphNode> findRoots(PrimaryAPIUsageGraph primaryAPIUsageGraph){
        List<DirectedGraphNode> roots = new ArrayList<>();
        Iterator<DirectedGraphNode> iterator = primaryAPIUsageGraph.iterator();
        while( iterator.hasNext() ){
            DirectedGraphNode graphNode = iterator.next();
            if( primaryAPIUsageGraph.getPredNodeCount( graphNode ) == 0 )
                roots.add( graphNode );
        }
        return roots;
    }

    static List<DirectedGraphNode> findLeaves(PrimaryAPIUsageGraph primaryAPIUsageGraph){
        List<DirectedGraphNode> leaves = new ArrayList<>();
        Iterator<DirectedGraphNode> iterator = primaryAPIUsageGraph.iterator();
        while( iterator.hasNext() ){
            DirectedGraphNode graphNode = iterator.next();
            if( primaryAPIUsageGraph.getSuccNodeCount( graphNode ) == 0 )
                leaves.add( graphNode );
        }
        return leaves;
    }

    static void addStartAndEndNodes( PrimaryAPIUsageGraph primaryAPIUsageGraph ){
        List<DirectedGraphNode> roots = findRoots( primaryAPIUsageGraph );
        DirectedGraphNode newRoot = new NonFrameworkBoundaryNode( NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE );
        primaryAPIUsageGraph.addNode( newRoot );
        roots.forEach( root -> {
            primaryAPIUsageGraph.addEdge( newRoot, root, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        } );

        List<DirectedGraphNode> leaves = findLeaves( primaryAPIUsageGraph );
        DirectedGraphNode newLeaf = new NonFrameworkBoundaryNode( NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE );
        primaryAPIUsageGraph.addNode( newLeaf );
        leaves.forEach( leaf -> {
            primaryAPIUsageGraph.addEdge( leaf, newLeaf, PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        } );
    }






/*    PrimaryAPIUsageGraph createPartialPrimaryAPIUsageGraph(
                                   Graph<Statement> slicedCG,
                                   PrimaryAPIUsageGraphEdgeType dependencyType,
                                   Set<Statement> relevantStatements ) {
        PrimaryAPIUsageGraph partialPrimaryAPIUsageGraph = new PrimaryAPIUsageGraph(dependencyType);
        slicedCG.iterator().forEachRemaining( statement -> {
            try {
                DirectedGraphNode graphNode = toGraphNode( statement, relevantStatements );
                if( !partialPrimaryAPIUsageGraph.containsNode( graphNode ))
                    partialPrimaryAPIUsageGraph.addNode(  graphNode);
                slicedCG.getSuccNodes( statement ).forEachRemaining( statementSucc -> {
                    try {
                        DirectedGraphNode graphNodeSucc = toGraphNode( statementSucc, relevantStatements );
                        if( !partialPrimaryAPIUsageGraph.containsNode( graphNodeSucc ) )
                            partialPrimaryAPIUsageGraph.addNode( graphNodeSucc );
                        if( !partialPrimaryAPIUsageGraph.hasEdge( graphNode, graphNodeSucc, dependencyType ) )
                            partialPrimaryAPIUsageGraph.addEdge( graphNode, graphNodeSucc, dependencyType );
                    } catch (FrameworkUtilityNotFoundException e) {
                        e.printStackTrace();
                    }
                } );
            } catch (FrameworkUtilityNotFoundException e) {
                e.printStackTrace();
            }
        } );
        return partialPrimaryAPIUsageGraph;

    }*/

/*    void addToPrimaryAPIUsageGraph(SlowSparseNumberedLabeledGraph<DirectedGraphNode, PrimaryAPIUsageGraphEdgeType> labeledGraph,
                                   SDG<InstanceKey> sdg, CGNode entrypoint,
                                   PrimaryAPIUsageGraphEdgeType dependencyType, Set<Statement> relevantStatements ) throws FrameworkUtilityNotFoundException {
        Statement root = new NormalStatement( entrypoint, entrypoint.getIR().iterateAllInstructions().next().iIndex() );
        try {
            Collection<Statement> statements = Slicer.computeForwardSlice(controlOnlySdg, root);
            statements.forEach( statement -> System.out.println( statement.toString() ) );
        } catch (CancelException e) {
            e.printStackTrace();
        }
        Set<Statement> visitedStatements = new HashSet<>();
        Queue<Statement> queue = new LinkedList<>();
        queue.add( root );
        while ( !queue.isEmpty() ){
            Statement top = queue.poll();
            if( visitedStatements.contains( top ) )
                continue;
            visitedStatements.add( top );

            DirectedGraphNode topToGraphNode = toGraphNode( top, relevantStatements );
            if( !labeledGraph.containsNode( topToGraphNode ) )
                labeledGraph.addNode( topToGraphNode );
            sdg.getSuccNodes( top ).forEachRemaining( topSucc -> {
                try {
                    DirectedGraphNode topSuccToGraphNode = toGraphNode( topSucc, relevantStatements );
                    if( !labeledGraph.containsNode( topSuccToGraphNode ) )
                        labeledGraph.addNode( topSuccToGraphNode );
                    labeledGraph.addEdge( topToGraphNode, topSuccToGraphNode, dependencyType );

                    // If this statement is an application statement, then, add it to the queue for further exploration
                    if(topSucc.getNode().getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Application") ||
                            topSucc.getNode().getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Source"))
                        queue.add( topSucc );

                } catch (FrameworkUtilityNotFoundException e) {
                    e.printStackTrace();
                }
            } );
        }
    }*/



    static DirectedGraphNode toGraphNode(Statement statement, Set<Statement> relevantStatements, FrameworkUtils frameworkUtils, Map<Statement, DirectedGraphNode> statementDirectedGraphNodeMap ) throws FrameworkUtilityNotFoundException {
//        if(statement.toString().contains("LoginContext") && statement.toString().contains("TestClass"))
//            System.out.print("");
        if( statementDirectedGraphNodeMap.containsKey( statement ) )
            return statementDirectedGraphNodeMap.get( statement );
        DirectedGraphNode directedGraphNode = null;
        if( relevantStatements.contains( statement ) )
            directedGraphNode = new FrameworkRelatedNode( toStatementRepresentation( statement, frameworkUtils ) );
        else
            directedGraphNode = new NonFrameworkMiddleNode( toStatementRepresentation( statement, frameworkUtils ) );
        statementDirectedGraphNodeMap.put( statement, directedGraphNode );
        return directedGraphNode;
    }



/*    List<Statement> findSDGRoots( SDG<InstanceKey> sdg ){
        System.out.println("Finding SDG roots...");
        List<Statement> roots = sdg.stream().filter( statement -> sdg.getPredNodeCount( statement ) == 0 ).collect(Collectors.toList());
        System.out.println( "\t" + roots.size() + " roots been found!" );
        return roots;
    }*/

    static Set<Statement> findRelevantStatements(String framework, CallGraph callGraph) throws Exception {
        CommonConstants.LOGGER.log( Level.FINE, "\tFinding relevant statements" );

        // Finding relevant nodes using SDG
        Predicate<Statement> filter = getPredicate(framework);
        Set<Statement> relevantStatements = new HashSet<>();

        // Finding relevant nodes using CallGraph
        callGraph.iterator().forEachRemaining(cgNode -> {
            boolean considerNode =
                    ( cgNode.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) ||
                    cgNode.getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Source") ) &&
                            cgNode.getIR() != null;
            if (considerNode) {
                cgNode.getIR().iterateNormalInstructions().forEachRemaining(ssaInstruction -> {
                    try {
                        if (isRelevantInstruction(framework, ssaInstruction, cgNode)) {
                            relevantStatements.add(new NormalStatement(cgNode, ssaInstruction.iIndex()));
                        }
                    } catch (FrameworkUtilityNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            }
        });


//        relevantStatements.forEach( statement -> System.out.println( "\t\t" + statement ) );
        CommonConstants.LOGGER.log( Level.FINE,  " -> " + relevantStatements.size() + " relevant statement(s) were found!" );
        return relevantStatements;
    }

/*    Set<Statement> findRelevantStatements(String framework, SDG<InstanceKey> sdg) throws Exception {
        System.out.println("Finding relevant statements...");
        // Finding relevant nodes using SDG
        Predicate<Statement> filter = getPredicate(framework);
        Set<Statement> relevantStatements = new HashSet<>();
        try {
            Iterator<Statement> itr = sdg.iterator();
            while (itr.hasNext()) {
                Statement statement = itr.next();
                try {
                    if (filter.test(statement))
                        if (!relevantStatements.contains(statement)) {
                            relevantStatements.add(statement);
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        relevantStatements.forEach( statement -> System.out.println( "\t" + statement ) );
        return relevantStatements;
    }
*/
    private static Predicate<Statement> getPredicate(String framework) throws Exception {
        return new FrameworkFilter( framework );
    }

    static boolean isRelevantInstruction(String framework, SSAInstruction ssaInstruction, CGNode cgNode) throws FrameworkUtilityNotFoundException {

        if (!ssaInstruction
                .toString()
                .matches(
                        "^(?=.*(Source|Application).*)(?!.*Primordial).*"))
            return false;

        try {
            if (!cgNode.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)
                    &&
                    !cgNode.getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Source")
                // Below line added by A.Shokri
                //|| !(s instanceof NormalStatement)
            ) {
                return false;
            }

/*
            if( ssaInstruction.toString().contains( "getName()" ) )
                System.out.print("");
*/

            IClass c = WalaUtils.getTargetClass(ssaInstruction, cgNode);

            if( c == null )
                return false;

            FrameworkUtils frameworkUtils = FrameworkUtilsFactory.getFrameworkUtils(framework);

            boolean classFromFramework = frameworkUtils.isFromFramework(c);
            boolean classInheritsFromFramework = frameworkUtils.inheritsFromFramework(c);

            if( !classFromFramework && !classInheritsFromFramework )
                return false;

            // This method is either in the framework or is defined in a class inheriting/implementing a framework interface or class.

            NormalStatement normalStatement = new NormalStatement(cgNode, ssaInstruction.iIndex());

            if( WalaUtils.isNewObjectNode( normalStatement ) || WalaUtils.isConstructorNode(normalStatement) )
                // It is an instantiation instruction for a framework related class (directly/indirectly)
                //TODO: Find a solution for the situation that the constructor is not from the framework
                /*
                Case 1:
                Interface FrameworkClass1{...}
                Class FrameworkClass1Implementor implements FrameworkClass1{
                    public FrameworkClass1Implementor( int i, String s, .... ){...}
                }

                Case 2:
                Class FrameworkClass1{
                    public FrameworkClass1(){...}
                }

                Class FrameworkClass1Extender extends FrameworkClass1{
                    public FrameworkClass1Extender( int i, String s, .... ){...}
                }
                */
                return true;

            String m = WalaUtils.getTargetMethod( normalStatement );

            if( ( classFromFramework && frameworkUtils.isFromFramework(c, m)) ||
                    (classInheritsFromFramework && frameworkUtils.inheritsFromFramework(c, m))
            )
                // This method is defined in a framework class/interface
                return true;

/*            if( classInheritsFromFramework && WalaUtils.isConstructorNode( normalStatement ) ){
                // It is a constructor which is not directly coming from framework
                // If all the super classes from framework are interfaces (and so there is no constructor from super classes
                // that could be used while instantiating an object of this class), then, accept this constructor as part of the framework.
                for( IClass superClassFromFramework : frameworkUtils.getSuperClassesFromFramework(c) ){
                    if( !(superClassFromFramework.isInterface() || superClassFromFramework.isAbstract()) )
                        return false;
                }
                return true;
            }*/

//            return c != null && (frameworkUtils.isFromFramework(c) || frameworkUtils.inheritsFromFramework(c));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
/**
     * Gets the target class for method invocations and objects instantiations.
     *
     * @param stmt the statement
     * @return the target class (or null if the instruction is not a method invocation or an object instantiation)
     */
    public static IClass getTargetClass(Statement stmt) {
        IClassHierarchy cha = stmt.getNode().getClassHierarchy();
        if (stmt.getKind() == Statement.Kind.NORMAL) {
            NormalStatement normalStmt = (NormalStatement) stmt;
            if (normalStmt.getInstruction() instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) normalStmt.getInstruction();
                return cha.lookupClass(invokeIns.getDeclaredTarget().getDeclaringClass());
            } else if (normalStmt.getInstruction() instanceof SSANewInstruction) {
                SSANewInstruction newIns = (SSANewInstruction) normalStmt.getInstruction();
                return cha.lookupClass(newIns.getNewSite().getDeclaredType());
            }
        }
        return null;
    }

    public static StatementRepresentation toStatementRepresentation( Statement statement, FrameworkUtils frameworkUtils ){
        String originClass = statement.getNode().getMethod().getDeclaringClass().getName().toString();//getSignature().split("\\.")[0];
        String originMethod = statement.getNode().getMethod().getSelector().toString();//getSignature().split("\\.")[1];
        int originalLineNumber = ((NormalStatement)statement).getNode().getIR().getMethod().getLineNumber( ((NormalStatement) statement).getInstructionIndex() );
        IClass targetClass = WalaUtils.getTargetClass( statement );
        if( targetClass == null )
            return new StatementRepresentation("Unknown", "Unknown", false, false, true,
                    originClass , originMethod, originalLineNumber, StatementRepresentation.ApiType.UNKNOWN,
                    ((NormalStatement) statement).getInstructionIndex(), statement.getNode().getGraphNodeId() /*System.identityHashCode (statement.getNode())*/);
        IClass frameworkClass = frameworkUtils.getFrameworkType( targetClass );
        String frameworkClassStr = frameworkClass.getName().toString();
        // To remove the letter "L" from the beginning of the class name (e.g. Ljava.... -> java...)
        frameworkClassStr = frameworkClassStr.substring(1);
        String frameworkMethod = WalaUtils.getTargetMethod( statement );

        StatementRepresentation.ApiType apiType = StatementRepresentation.ApiType.INVOCATION;
        boolean isAbstractOrInterface = WalaUtils.isAbstractOrInterfaceConstructor( frameworkClass );
        boolean isStaticMethod = WalaUtils.isStaticMethod( statement );
        boolean isPublicMethod = WalaUtils.isPublicMethod( statement );
        if( WalaUtils.isNewObjectNode( statement ) )
            apiType = StatementRepresentation.ApiType.NEW_OBJECT;
        if ( WalaUtils.isConstructorNode(statement) )
            apiType = StatementRepresentation.ApiType.CONSTRUCTOR;
        StatementRepresentation statementRepresentation = new StatementRepresentation(frameworkClassStr, frameworkMethod,
                isAbstractOrInterface, isStaticMethod, isPublicMethod,
                originClass, originMethod, originalLineNumber, apiType,
                ((NormalStatement) statement).getInstructionIndex(), System.identityHashCode (statement.getNode()));
        return statementRepresentation;
    }

}
