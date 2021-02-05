package edu.rit.se.design.patternMining.mapo;

import com.ibm.wala.util.collections.HashSetFactory;
/*import edu.rit.se.design.Constants;
import edu.rit.se.design.analysis.ProjectAnalysisFactory;
import edu.rit.se.design.evaluation.IModelEvaluationResult;
import edu.rit.se.design.evaluation.LeaveOutExperimentResult;
import edu.rit.se.design.evaluation.missedAPI.mapo.MapoMissedApiEvaluation;
import edu.rit.se.design.evaluation.missedAPI.mapo.MapoMissedApiEvaluationResult;
import edu.rit.se.design.evaluation.modelCreation.mapo.MapoPatternsCreationEvaluation;
import edu.rit.se.design.evaluation.modelCreation.mapo.MapoPatternsEvaluationResult;
import edu.rit.se.design.evaluation.nextAPI.mapo.MapoNextApiEvaluation;
import edu.rit.se.design.evaluation.nextAPI.mapo.MapoNextApiEvaluationResult;
import edu.rit.se.design.evaluation.swappedAPI.mapo.MapoSwappedApiEvaluation;
import edu.rit.se.design.evaluation.swappedAPI.mapo.MapoSwappedApiEvaluationResult;
import edu.rit.se.design.graphs.fsm.FsmEdge;
import edu.rit.se.design.graphs.grasp.*;*/

import java.io.IOException;
import java.util.*;

public class MapoExperimentBK {
/*
    public static void main(String[] args) throws Exception {

        Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes =
//                        null;
                new Class[]{GraspEndNode.class};

//        String trainGraspFolderPaths = "D:/RIT-Sync/Research/Projects/SpecMiner/Data/JAAS/Working/Train";

        ProjectAnalysisFactory.Framework framework = ProjectAnalysisFactory.Framework.JAAS;
        String trainGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train";
        String testGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/edu.rit.se.design.fspec2code.Test";

//        ProjectAnalysisFactory.Framework framework = ProjectAnalysisFactory.Framework.RMI;
//        String trainGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/50ProjsJar/Train";
//        String testGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/50ProjsJar/edu.rit.se.design.fspec2code.Test";


//        trainTrainExperiment( trainGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );
//        leaveOneOutExperiment( trainGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );



        trainTestExperiment( trainGraspFolderPaths, testGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );

//        leaveOutExperiment( 2, trainGraspFolderPaths, testGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );







     */
/*   long timer = System.currentTimeMillis();

        CodeRepository codeRepository = new CodeRepository(trainGraspBuilder.getProjectNameGraspListMap());
        MapoCodeAnalyzer mapoCodeAnalyzer = new MapoCodeAnalyzer( codeRepository );
        System.out.println( mapoCodeAnalyzer.report() );
//        System.out.println( MapoClusteringEngine.similarityReport( mapoCodeAnalyzer.getAllMapoApiCallSequences() ) );

        MAPOClusteringEngine mapoClusteringEngine = new MAPOClusteringEngine();

        ClusterNode cluster =  mapoClusteringEngine.binaryCluster( mapoCodeAnalyzer.getAllMapoApiCallSequences() );
        List<ClusterNode> clusters = mapoClusteringEngine.toClusters( cluster, 12 );
        clusters.forEach( clusterNode -> System.out.println( clusterNode.toString() ) );

        MapoApiSeqCallMiner mapoApiSeqCallMiner = new MapoApiSeqCallMiner();

        mapoApiSeqCallMiner.findPatterns(clusters, .20);
        System.out.println( "Finding MAPO patterns finished in " + ((System.currentTimeMillis() - timer ) ) + " milli seconds" );

        mapoApiSeqCallMiner.report();*//*


    }

    static Set<Set<Grasp>> generateLeaveOuts( List<Grasp> allGraspList, int leaveNumber){
        Set<Set<Grasp>> combinations = new HashSet<>();
        allGraspList.forEach( grasp -> {
            Set<Grasp> initialSet = new HashSet<>();
            initialSet.add( grasp );
            combinations.add( initialSet );
        } );

        for( int i = 1; i < leaveNumber; i++ ){
            Set<Set<Grasp>> newCombinations = new HashSet<>();
            allGraspList.forEach( grasp -> {
                combinations.forEach(combination -> {
                    if( !combination.contains( grasp ) ){
                        Set<Grasp> newCombination = new HashSet<>( combination );
                        newCombination.add( grasp );
                        newCombinations.add( newCombination );
                    }
                });
            } );
            combinations.clear();
            combinations.addAll( newCombinations );
            newCombinations.clear();
        }
        return combinations;
    }

    static void leaveOutExperiment( int leaveOut, String trainGraspFolderPaths, String testGraspFolderPaths, ProjectAnalysisFactory.Framework framework, Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes ) throws IOException, ClassNotFoundException {
        GraspBuilder trainGraspBuilder = new GraspBuilder( framework, new HashMap<>());
//      trainGraspBuilder.createGraspsFromProjectsFolder( trainGraspFolderPaths, trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        trainGraspBuilder.createGraspFromSerializedGraspFiles( trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        GraspBuilder testGraspBuilder = new GraspBuilder( framework, new HashMap<>());
//        testGraspBuilder.createGraspsFromProjectsFolder( testGraspFolderPaths, testGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        testGraspBuilder.createGraspFromSerializedGraspFiles( testGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        Set<GraspEdge.GraspEdgeType> graspEdgeTypes = HashSetFactory.make();
        graspEdgeTypes.add(GraspEdge.GraspEdgeType.DOMINANCE);
        Set<FsmEdge.FsmEdgeType> fsmEdgeTypes = HashSetFactory.make();
        fsmEdgeTypes.add(FsmEdge.FsmEdgeType.DOMINANCE);

        List<Grasp> allGraspList = new ArrayList<>();
        allGraspList.addAll( trainGraspBuilder.getAllBuiltGrasps() );
        allGraspList.addAll( testGraspBuilder.getAllBuiltGrasps() );

        LeaveOutExperimentResult leaveOutExperimentResult = null;

        Set<Set<Grasp>> leaveOutCombinations = generateLeaveOuts(allGraspList, leaveOut);

        int combinationCounter = 0;
        for( Set<Grasp> testGrasps: leaveOutCombinations ) {
            combinationCounter++;
            List<Grasp> trainGrasps = new ArrayList<>( allGraspList );
            trainGrasps.removeAll( testGrasps );
            List<Grasp> testGraspList = new ArrayList<>( testGrasps );

            StringBuilder testGraphsTitle = new StringBuilder();
            testGrasps.forEach( grasp -> testGraphsTitle.append( grasp.getTitle() + (testGraphsTitle.length() == 0 ? "" : ",") ) );
            System.out.println( combinationCounter + "/" + leaveOutCombinations.size() + ": Grasp(s) \"" + testGraphsTitle + "\" is/are left out" );

            Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> clonedTrainGraspMap =
                    createTrainGraspMap( testGraspList, trainGraspBuilder.getProjectNameGraspListMap(), testGraspBuilder.getProjectNameGraspListMap() );

            MapoPatternsCreationEvaluation kfoldMapoPatternsCreationEvaluation = new MapoPatternsCreationEvaluation(
                    clonedTrainGraspMap, 12, .2);
            MapoPatternsEvaluationResult kfoldMmapoPatternsEvaluationResult = kfoldMapoPatternsCreationEvaluation.evaluate();
//            System.out.println( kfoldMmapoPatternsEvaluationResult.getEvaluationResult() );

            MapoNextApiEvaluation kfoldMapoNextApiEvaluation = new MapoNextApiEvaluation(kfoldMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
            MapoNextApiEvaluationResult kfoldMapoNextApiEvaluationResult = kfoldMapoNextApiEvaluation.evaluate();
//            System.out.println( kfoldMapoNextApiEvaluationResult.getEvaluationResult() );

            MapoMissedApiEvaluation kfoldMapoMissedApiEvaluation = new MapoMissedApiEvaluation(kfoldMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
            MapoMissedApiEvaluationResult kfoldMapoMissedApiEvaluationResult = kfoldMapoMissedApiEvaluation.evaluate();
//            System.out.println( kfoldMapoMissedApiEvaluationResult.getEvaluationResult() );

            MapoSwappedApiEvaluation kfoldMapoSwappedApiEvaluation = new MapoSwappedApiEvaluation(kfoldMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
            MapoSwappedApiEvaluationResult kfoldMapoSwappedApiEvaluationResult = kfoldMapoSwappedApiEvaluation.evaluate();
//            System.out.println( kfoldMapoSwappedApiEvaluationResult.getEvaluationResult() );


            LeaveOutExperimentResult newResult = new LeaveOutExperimentResult( new IModelEvaluationResult[]{kfoldMmapoPatternsEvaluationResult, kfoldMapoNextApiEvaluationResult,
                    kfoldMapoMissedApiEvaluationResult, kfoldMapoSwappedApiEvaluationResult });
            if( leaveOutExperimentResult == null )
                leaveOutExperimentResult = newResult;
            else
                leaveOutExperimentResult.mergeResults( newResult  );

        }

        System.out.println( leaveOutExperimentResult.getEvaluationResult() );
    }

    static void trainTestExperiment(String trainGraspFolderPaths, String testGraspFolderPaths, ProjectAnalysisFactory.Framework framework, Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes) throws Exception {

        GraspBuilder trainGraspBuilder = new GraspBuilder( framework, new HashMap<>());
//      trainGraspBuilder.createGraspsFromProjectsFolder( trainGraspFolderPaths, trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        trainGraspBuilder.createGraspFromSerializedGraspFiles( trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        GraspBuilder testGraspBuilder = new GraspBuilder( framework, new HashMap<>());
//        testGraspBuilder.createGraspsFromProjectsFolder( testGraspFolderPaths, testGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        testGraspBuilder.createGraspFromSerializedGraspFiles( testGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        List<Grasp> testGraspList = testGraspBuilder.getAllBuiltGrasps();

        System.out.println("MAPO Train-edu.rit.se.design.fspec2code.Test experiment results:");
        MapoPatternsCreationEvaluation trainTestMapoPatternsCreationEvaluation = new MapoPatternsCreationEvaluation(
                testGraspBuilder.getProjectNameGraspListMap(), 12, .2);
        MapoPatternsEvaluationResult trainTestMmapoPatternsEvaluationResult = trainTestMapoPatternsCreationEvaluation.evaluate();
            System.out.println( trainTestMmapoPatternsEvaluationResult.getEvaluationResult() );

        MapoNextApiEvaluation trainTestMapoNextApiEvaluation = new MapoNextApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoNextApiEvaluationResult trainTestMapoNextApiEvaluationResult = trainTestMapoNextApiEvaluation.evaluate();
            System.out.println( trainTestMapoNextApiEvaluationResult.getEvaluationResult() );

        MapoMissedApiEvaluation trainTestMapoMissedApiEvaluation = new MapoMissedApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoMissedApiEvaluationResult trainTestMapoMissedApiEvaluationResult = trainTestMapoMissedApiEvaluation.evaluate();
            System.out.println( trainTestMapoMissedApiEvaluationResult.getEvaluationResult() );

        MapoSwappedApiEvaluation trainTestMapoSwappedApiEvaluation = new MapoSwappedApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoSwappedApiEvaluationResult trainTestMapoSwappedApiEvaluationResult = trainTestMapoSwappedApiEvaluation.evaluate();
            System.out.println( trainTestMapoSwappedApiEvaluationResult.getEvaluationResult() );

    }



    static void trainTrainExperiment(String trainGraspFolderPaths, ProjectAnalysisFactory.Framework framework, Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes) throws IOException, ClassNotFoundException {
        GraspBuilder trainGraspBuilder = new GraspBuilder( framework, new HashMap<>());

//                trainGraspBuilder.createGraspsFromProjectsFolder( trainGraspFolderPaths, trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        trainGraspBuilder.createGraspFromSerializedGraspFiles( trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        MapoPatternsCreationEvaluation mapoPatternsCreationEvaluation = new MapoPatternsCreationEvaluation(
                trainGraspBuilder.getProjectNameGraspListMap(), 12, .2);
        MapoPatternsEvaluationResult mapoPatternsEvaluationResult = mapoPatternsCreationEvaluation.evaluate();
//        System.out.println( mapoPatternsEvaluationResult.getEvaluationResult() );

        MapoNextApiEvaluation mapoNextApiEvaluation = new MapoNextApiEvaluation(mapoPatternsCreationEvaluation.getFoundPatterns(), trainGraspBuilder.getAllBuiltGrasps());
        MapoNextApiEvaluationResult mapoNextApiEvaluationResult = mapoNextApiEvaluation.evaluate();
        System.out.println( mapoNextApiEvaluationResult.getEvaluationResult() );

        MapoMissedApiEvaluation mapoMissedApiEvaluation = new MapoMissedApiEvaluation(mapoPatternsCreationEvaluation.getFoundPatterns(), trainGraspBuilder.getAllBuiltGrasps());
        MapoMissedApiEvaluationResult mapoMissedApiEvaluationResult = mapoMissedApiEvaluation.evaluate();
        System.out.println( mapoMissedApiEvaluationResult.getEvaluationResult() );

        MapoSwappedApiEvaluation mapoSwappedApiEvaluation = new MapoSwappedApiEvaluation(mapoPatternsCreationEvaluation.getFoundPatterns(), trainGraspBuilder.getAllBuiltGrasps());
        MapoSwappedApiEvaluationResult mapoSwappedApiEvaluationResult = mapoSwappedApiEvaluation.evaluate();
        System.out.println( mapoSwappedApiEvaluationResult.getEvaluationResult() );
    }

    static Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> cloneGraspMap(Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> originalGraspMap ){
        Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> clonedMap = new HashMap<>();
        originalGraspMap.forEach((s, grasps) -> clonedMap.put( s, new ArrayList<>(grasps) ) );
        return clonedMap;
    }

    static public Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> createTrainGraspMap(List<Grasp> testGrasps,
            Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> originalTrainFolderGraspMap,
             Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> originalTestFolderGraspMap ) {
        Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> trainFolderClonedMap = cloneGraspMap( originalTrainFolderGraspMap );
        Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> testFolderClonedMap = cloneGraspMap( originalTestFolderGraspMap );

        Set<String> keySet = new HashSet<>( trainFolderClonedMap.keySet() );
        keySet.forEach( s -> trainFolderClonedMap.get(s).removeAll(testGrasps) );
        keySet.forEach( s -> {if( trainFolderClonedMap.get(s).isEmpty() ) trainFolderClonedMap.remove(s);} );

        keySet = new HashSet<>( testFolderClonedMap.keySet() );
        keySet.forEach( s -> testFolderClonedMap.get(s).removeAll(testGrasps) );
        keySet.forEach( s -> {if( testFolderClonedMap.get(s).isEmpty() ) testFolderClonedMap.remove(s);} );

        Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> combinedMap = new HashMap<>();
        combinedMap.putAll( trainFolderClonedMap );
        testFolderClonedMap.forEach( (projectName, grasps) -> {
            if( !combinedMap.containsKey( projectName ) )
                combinedMap.put( projectName, new ArrayList<>() );
            combinedMap.get( projectName ).addAll( grasps );
        } );

        return combinedMap;
    }
*/


}
