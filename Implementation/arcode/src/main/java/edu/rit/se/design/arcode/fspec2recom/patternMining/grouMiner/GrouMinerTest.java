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

package edu.rit.se.design.arcode.fspec2recom.patternMining.grouMiner;

/*import edu.rit.se.design.Constants;
import edu.rit.se.design.analysis.CLI;
import edu.rit.se.design.analysis.ProjectAnalysisFactory;
import edu.rit.se.design.evaluation.missedAPI.GrouMiner.GrouMinerMissedApiEvaluation;
import edu.rit.se.design.evaluation.missedAPI.GrouMiner.GrouMinerMissedApiEvaluationResult;
import edu.rit.se.design.evaluation.nextAPI.grouMiner.GrouMinerNextApiEvaluation;
import edu.rit.se.design.evaluation.nextAPI.grouMiner.GrouMinerNextApiEvaluationResult;
import edu.rit.se.design.evaluation.swappedAPI.grouMiner.GrouMinerSwappedApiEvaluation;
import edu.rit.se.design.evaluation.swappedAPI.grouMiner.GrouMinerSwappedApiEvaluationResult;
import edu.rit.se.design.graphs.grasp.GraspBuilder;
import edu.rit.se.design.graphs.grasp.GraspEndNode;
import edu.rit.se.design.graphs.grasp.GraspNode;
import edu.rit.se.design.utils.FrameworkUtilityNotFoundException;
import edu.rit.se.design.utils.FrameworkUtils;
import edu.rit.se.design.utils.FrameworkUtilsFactory;
import edu.rit.se.design.utils.LoggerUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;*/

public class GrouMinerTest {
/*    public static void main(String[] args) throws Exception {

        Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes =
//                        null;
                new Class[]{GraspEndNode.class};

//        String trainGraspFolderPaths = "D:/RIT-Sync/Research/Projects/SpecMiner/Data/JAAS/Working/Train";

//        ProjectAnalysisFactory.Framework framework = ProjectAnalysisFactory.Framework.JAAS;
//        String trainGrouMinerMapFilePath = "/Users/ali/Academic/RIT/Research/Paper Submission/EMSE_SpecialIssue/Data/GrouMiner_Patterns_JAAS";
//        String testGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Test";

        ProjectAnalysisFactory.Framework framework = ProjectAnalysisFactory.Framework.RMI;
        String trainGrouMinerMapFilePath = "/Users/ali/Academic/RIT/Research/Paper Submission/EMSE_SpecialIssue/Data/GrouMiner_Patterns_RMI";
        String testGraspFolderPaths = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/50ProjsJar/Test";


//        trainTrainExperiment( trainGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );
//        leaveOneOutExperiment( trainGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );
        trainTestExperiment( trainGrouMinerMapFilePath, testGraspFolderPaths, framework, toBeRemovedGraspNodeTypes );

    }

    static Map< Integer, List<Map<String, Set<String>>>> processRawGroumPatterns(Map<Integer, List<Pair<Set<String>, String>>> grouMinerPatternMap, FrameworkUtils frameworkUtils){
        Map< Integer, List<Map<String, Set<String>>>> processedGroumPatterns = new HashMap<>();
        List<String> frameworkClasses = new ArrayList<>();
        frameworkUtils.getFrameworkClasses().forEach( frameworkClass -> frameworkClasses.add(
                frameworkUtilsClassToGrouMinerFraneworkClass( frameworkClass ) ) );
        grouMinerPatternMap.forEach((frequency, pairs) -> {
            pairs.forEach( dotGraphNodeSetPair -> {
                String dotGraph = dotGraphNodeSetPair.getRight();
                Map<String, Set<String>> fromToGrouMinerNodeMap = new HashMap<>();
                Map<Integer, String> idlabelMap = new HashMap<>();
                for (String dotGraphLine : dotGraph.split("\n")) {
                    if (!(dotGraphLine.contains("{") || dotGraphLine.contains("->") || dotGraphLine.contains("}"))) {
                        // Node definition line
                        Integer labelId = Integer.parseInt(dotGraphLine.split(" ")[0]);
                        String nodeLabel = dotGraphLine.substring(dotGraphLine.indexOf("\"") + 1, dotGraphLine.lastIndexOf("\""));
                        idlabelMap.put(labelId, nodeLabel);
                    }
                }

                for (String dotGraphLine : dotGraph.split("\n")) {
                    if (!dotGraphLine.contains("->"))
                        continue;
                    String[] splitedLine = dotGraphLine.split(" ");
                    Integer fromId = Integer.parseInt(splitedLine[0]);
                    Integer toId = Integer.parseInt(splitedLine[2]);
                    if (idlabelMap.get(fromId) == null || idlabelMap.get(toId) == null)
                        continue;
                    if (!fromToGrouMinerNodeMap.containsKey(fromId + "_" + idlabelMap.get(fromId)))
                        fromToGrouMinerNodeMap.put(fromId + "_" + idlabelMap.get(fromId), new HashSet<>());
                    fromToGrouMinerNodeMap.get(fromId + "_" + idlabelMap.get(fromId)).add(toId + "_" + idlabelMap.get(toId));
                }

                removeNonFrameworkNodes(fromToGrouMinerNodeMap, frameworkClasses);
                if( fromToGrouMinerNodeMap.size() == 0 )
                    return;
                if( !processedGroumPatterns.containsKey(frequency) )
                    processedGroumPatterns.put(frequency, new ArrayList<>());
                processedGroumPatterns.get(frequency).add( fromToGrouMinerNodeMap );
            });
        });
        return processedGroumPatterns;

    }

    static void removeNonFrameworkNodes(Map<String, Set<String>> fromToGrouMinerNodeMap, List<String> frameworkClasses){
        Set<String> nonFrameworkNodes = new HashSet<>();
        fromToGrouMinerNodeMap.forEach((fromNode, toNodes) -> {
            if( !isFromFramework( fromNode, frameworkClasses ) )
                nonFrameworkNodes.add( fromNode );
            toNodes.forEach( toNode -> {
                if( !isFromFramework( toNode, frameworkClasses ) )
                    nonFrameworkNodes.add( toNode );
            } );
        });

        nonFrameworkNodes.forEach( nonFrameworkNode -> {
            Set<String> nonFrameworkNodeInputs = new HashSet<>();
            Set<String> nonFrameworkNodeOutputs = new HashSet<>();

            fromToGrouMinerNodeMap.forEach((fromNode, toNodes) -> {
                if (toNodes.contains(nonFrameworkNode))
                    nonFrameworkNodeInputs.add(fromNode);
                if (fromNode.equals(nonFrameworkNode))
                    nonFrameworkNodeOutputs.addAll(toNodes);
            });
            nonFrameworkNodeInputs.remove( nonFrameworkNode );
            nonFrameworkNodeOutputs.remove( nonFrameworkNode );
            // connecte inputs of nonframework node to its outputs
            nonFrameworkNodeInputs.forEach( nonFrameworkNodeInput -> {
                if(!fromToGrouMinerNodeMap.containsKey(nonFrameworkNodeInput))
                    fromToGrouMinerNodeMap.put(nonFrameworkNodeInput, new HashSet<>());
                fromToGrouMinerNodeMap.get(nonFrameworkNodeInput).addAll(nonFrameworkNodeOutputs);
            } );
            // removing nonFrameworkNode
            fromToGrouMinerNodeMap.remove(nonFrameworkNode);
            fromToGrouMinerNodeMap.keySet().forEach(fromNode1 -> fromToGrouMinerNodeMap.get(fromNode1).remove(nonFrameworkNode));
        } );

    }

    static boolean isFromFramework( String grouMinerNode, List<String> frameworkClasses ){
        String label = grouMinerNode.split("_")[1];
        label = label.startsWith("init ") ? label.substring(5) : label;
        label = label.replaceAll("\\(\\)", "" );

        return frameworkClasses.contains( label.contains(".") ? label.split("\\.")[0] : label );
    }

    static String frameworkUtilsClassToGrouMinerFraneworkClass(String frameworkClass){
        return frameworkClass.replaceAll("[a-zA-Z0-9]*\\/", "");
    }

    public static void trainTestExperiment( String trainGrouMinerMapFilePath, String testGraspFolderPaths,
                                     ProjectAnalysisFactory.Framework framework, Class<? extends GraspNode>[] toBeRemovedGraspNodeTypes) throws IOException, ClassNotFoundException, FrameworkUtilityNotFoundException {
        FileInputStream fileIn = new FileInputStream(trainGrouMinerMapFilePath);
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        LoggerUtils.getLogger( CLI.class ).fine( "Retrieving GrouMiner patterns from file " + trainGrouMinerMapFilePath  );
        Map<Integer, List<Pair<Set<String>, String>>> grouMinerPatternMap = (Map<Integer, List<Pair<Set<String>, String>>>) objectIn.readObject();
        objectIn.close();

        Map<Integer, List<Map<String, Set<String>>>> processedGroumPatterns = processRawGroumPatterns( grouMinerPatternMap, FrameworkUtilsFactory.getFrameworkUtils(framework) );

        GraspBuilder testGraspBuilder = new GraspBuilder( framework, new HashMap<>());
//      trainGraspBuilder.createGraspsFromProjectsFolder( trainGraspFolderPaths, trainGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME, AnalysisScopeFactory.ArtifactType.JAR_FILE);
        testGraspBuilder.createGraspFromSerializedGraspFiles( testGraspFolderPaths + "/" + Constants.GRASP.SERIALIZED_GRASP_FOLDER_NAME + "/" + Constants.GRASP.SERIALIZED_GRASP_SUB_FOLDER, toBeRemovedGraspNodeTypes );

        GrouMinerNextApiEvaluation grouMinerNextApiEvaluation = new GrouMinerNextApiEvaluation(processedGroumPatterns, testGraspBuilder.getAllBuiltGrasps(), FrameworkUtilsFactory.getFrameworkUtils( framework ));
        GrouMinerNextApiEvaluationResult grouMinerNextApiEvaluationResult = grouMinerNextApiEvaluation.evaluate();
        System.out.println( grouMinerNextApiEvaluationResult.getEvaluationResult() );

        GrouMinerMissedApiEvaluation grouMinerMissedApiEvaluation = new GrouMinerMissedApiEvaluation(processedGroumPatterns, testGraspBuilder.getAllBuiltGrasps(), FrameworkUtilsFactory.getFrameworkUtils( framework ));
        GrouMinerMissedApiEvaluationResult grouMinerMissedApiEvaluationResult = grouMinerMissedApiEvaluation.evaluate();
        System.out.println( grouMinerMissedApiEvaluationResult.getEvaluationResult() );
//
        GrouMinerSwappedApiEvaluation grouMinerSwappedApiEvaluation = new GrouMinerSwappedApiEvaluation(processedGroumPatterns, testGraspBuilder.getAllBuiltGrasps(), FrameworkUtilsFactory.getFrameworkUtils( framework ));
        GrouMinerSwappedApiEvaluationResult grouMinerSwappedApiEvaluationResult = grouMinerSwappedApiEvaluation.evaluate();
        System.out.println( grouMinerSwappedApiEvaluationResult.getEvaluationResult() );

    }*/
}
