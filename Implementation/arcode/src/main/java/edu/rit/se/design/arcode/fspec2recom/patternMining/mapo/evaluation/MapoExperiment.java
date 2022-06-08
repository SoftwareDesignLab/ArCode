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

package edu.rit.se.design.arcode.fspec2recom.patternMining.mapo.evaluation;

/*import com.ibm.wala.util.collections.HashSetFactory;
import edu.rit.se.design.Constants;
import edu.rit.se.design.analysis.ProjectAnalysisFactory;
import edu.rit.se.design.evaluation.IModelEvaluationResult;
import edu.rit.se.design.evaluation.LeaveOutExperimentResult;
import edu.rit.se.design.evaluation.TrainTestExperiment;
import edu.rit.se.design.evaluation.TrainTestExperimentResult;
import edu.rit.se.design.evaluation.missedAPI.mapo.MapoMissedApiEvaluation;
import edu.rit.se.design.evaluation.missedAPI.mapo.MapoMissedApiEvaluationResult;
import edu.rit.se.design.evaluation.missedAPI.specMiner.FsmMissedApiEvaluationResult;
import edu.rit.se.design.evaluation.modelCreation.mapo.MapoPatternsCreationEvaluation;
import edu.rit.se.design.evaluation.modelCreation.mapo.MapoPatternsEvaluationResult;
import edu.rit.se.design.evaluation.modelCreation.specMiner.FsmSaturationEvaluationResult;
import edu.rit.se.design.evaluation.nextAPI.mapo.MapoNextApiEvaluation;
import edu.rit.se.design.evaluation.nextAPI.mapo.MapoNextApiEvaluationResult;
import edu.rit.se.design.evaluation.nextAPI.specMiner.FsmNextApiEvaluationResult;
import edu.rit.se.design.evaluation.swappedAPI.mapo.MapoSwappedApiEvaluation;
import edu.rit.se.design.evaluation.swappedAPI.mapo.MapoSwappedApiEvaluationResult;
import edu.rit.se.design.evaluation.swappedAPI.specMiner.FsmSwappedApiEvaluationResult;
import edu.rit.se.design.graphs.fsm.FsmEdge;
import edu.rit.se.design.graphs.grasp.*;
import edu.rit.se.design.arcode.fspec2recom.patternMining.generic.CodeRepository;
import edu.rit.se.design.sdg.AnalysisScopeFactory;*/
import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
/*
import edu.rit.se.design.utils.LoggerUtils;
import edu.rit.se.design.arcode.fspec2recom.clustering.ClassicalClusteringEngine;
import edu.rit.se.design.arcode.fspec2recom.clustering.ClusterNode;

import java.io.IOException;
*/
import java.util.*;

public class MapoExperiment {
    public static void main(String[] args) throws Exception {

        Class<? extends DirectedGraphNode>[] toBeRemovedGraspNodeTypes =
//                        null;
                new Class[]{NonFrameworkBoundaryNode.class};

        String framework = "JAAS";
        String trainGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train";
        String testGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/edu.rit.se.design.arcode.fspec2code.Test";

        trainTestExperiment( trainGraspFolderPaths, testGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );

    }

    static Map<String, List<GRAAM>> graamMapGenerator( List<GRAAM> graamList ){
        Map<String, List<GRAAM>> projectGraamMap = new HashMap<>();
        graamList.forEach( graam -> {
            if( !projectGraamMap.containsKey(graam.getProjectPath()) )
                projectGraamMap.put( graam.getProjectPath(), new ArrayList<>() );
            projectGraamMap.get( graam.getProjectPath() ).add( graam );
        } );
        return projectGraamMap;
    }

    static void trainTestExperiment(String trainGraspFolderPaths, String testGraspFolderPaths, String framework, Class<? extends DirectedGraphNode>[] toBeRemovedGraspNodeTypes) throws Exception {

        String serializedTrainGraspFolderPath = trainGraspFolderPaths + "/" + "SerializedGRAAMs";
        String serializedTestGraspFolderPath = testGraspFolderPaths + "/" + "SerializedGRAAMs";

        String serializedTrainPAUGFolderPath = trainGraspFolderPaths + "/" + "SerializedPAUGs";
        String serializedTestPAUGFolderPath = testGraspFolderPaths + "/" + "SerializedPAUGs";

        List<PrimaryAPIUsageGraph> trainPAUGList = PrimaryAPIGraphBuilder.loadPAGsFromSerializedFolder( serializedTrainPAUGFolderPath );


        List<GRAAM> trainGraspList = GRAAMBuilder.loadGRAAMsFromSerializedFolder( serializedTrainGraspFolderPath );
        List<GRAAM> testGraspList = GRAAMBuilder.loadGRAAMsFromSerializedFolder( serializedTestGraspFolderPath );

        System.out.println("MAPO Train-edu.rit.se.design.arcode.fspec2code.Test experiment results:");

        MapoPatternsCreationEvaluation trainTestMapoPatternsCreationEvaluation = new MapoPatternsCreationEvaluation(
                graamMapGenerator(trainGraspList), 12, .2);
        MapoPatternsEvaluationResult trainTestMmapoPatternsEvaluationResult = trainTestMapoPatternsCreationEvaluation.evaluate();
//        System.out.println( trainTestMmapoPatternsEvaluationResult.getEvaluationResult() );

/*
        MapoNextApiEvaluation trainTestMapoNextApiEvaluation = new MapoNextApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoNextApiEvaluationResult trainTestMapoNextApiEvaluationResult = trainTestMapoNextApiEvaluation.evaluate();
            System.out.println( trainTestMapoNextApiEvaluationResult.getEvaluationResult() );
*/


        MapoMissedApiEvaluation trainTestMapoMissedApiEvaluation = new MapoMissedApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoMissedApiEvaluationResult trainTestMapoMissedApiEvaluationResult = trainTestMapoMissedApiEvaluation.evaluate();
            System.out.println( trainTestMapoMissedApiEvaluationResult.getEvaluationResult() );


/*
        MapoSwappedApiEvaluation trainTestMapoSwappedApiEvaluation = new MapoSwappedApiEvaluation(trainTestMapoPatternsCreationEvaluation.getFoundPatterns(), testGraspList);
        MapoSwappedApiEvaluationResult trainTestMapoSwappedApiEvaluationResult = trainTestMapoSwappedApiEvaluation.evaluate();
            System.out.println( trainTestMapoSwappedApiEvaluationResult.getEvaluationResult() );
*/

    }


 /*   static Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> cloneGraspMap(Map<String, List<Grasp<GraspNode, GraspEdge, IGraspEdgeType, GraspEdgeAux<GraspNode, GraspEdge>>>> originalGraspMap ){
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
    }*/


}
