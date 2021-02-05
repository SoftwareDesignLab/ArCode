package edu.rit.se.design.arcode.fspec2recom;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class Test {

    public static void main(String[] args) throws IOException, ClassNotFoundException, ClassHierarchyException {
        String framework =
                "JAAS";

        String fSpecFilePath =
                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/FSpec.spmn";

        String serializedTrainGRAAMsFolderPath =
//                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train/SerializedGRAAMs";
                "/Users/ali/Academic/RIT/Research/Projects/ArCode/DataRepository/JAAS/Train/SerializedGRAAMs";

        String serializedTestGRAAMsFolderPath =
                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/edu.rit.se.design.arcode.fspec2code.Test/SerializedGRAAMs";

        String classHierarchyExclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
        String frameworkJarFile = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";

//        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarFile, classHierarchyExclusionFile);

/*        String framework =
                "JAAS";
//                "RMI";

        String frameworkJarPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";
        String frameworkPackage = "javax/security/auth";


        IFDBuilder ifdBuilder =  new IFDBuilder();
        IFD ifd =
//                null;
                ifdBuilder.buildIFD( framework, frameworkJarPath, frameworkPackage );*/

        List<GRAAM> loadedGRAAMs = GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTrainGRAAMsFolderPath);
//         TODO: remove below line. its just to see what would happen if we double the size of train data
//        loadedGRAAMs.addAll( loadedGRAAMs );
        loadedGRAAMs.addAll( GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTestGRAAMsFolderPath) );

        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();

        for (int i = 0; i < loadedGRAAMs.size(); i++) {
            GRAAM leftOutGraam = loadedGRAAMs.get(i);
            List<GRAAM> trainGRAAMs = new ArrayList<>( loadedGRAAMs );
            trainGRAAMs.remove( leftOutGraam );

            List<GRAAM> testGRAAMs = new ArrayList<>();
            testGRAAMs.add( leftOutGraam );

            FSpec fSpec =  FSpecBuilder.buildFSpec( framework, trainGRAAMs  );
            Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounterRes =
                    runNextAPIExperiment( testGRAAMs, fSpec, 8 );
            matchedRecommendationRankCounterRes.forEach( (exp_no, rankMap) -> {
                rankMap.forEach( (rank, qty) -> {
                    if (!matchedRecommendationRankCounter.containsKey(exp_no))
                        matchedRecommendationRankCounter.put( exp_no, new HashMap<>() );
                    if( !matchedRecommendationRankCounter.get( exp_no ).containsKey( rank ) )
                        matchedRecommendationRankCounter.get( exp_no ).put( rank, 0 );
                    matchedRecommendationRankCounter.get( exp_no ).put( rank,
                            matchedRecommendationRankCounter.get( exp_no ).get( rank ) + qty );
                } );
            } );
        }


        printExperimentResult( matchedRecommendationRankCounter, "Next API Recommendation - 1-Left-Out", 30 );

    }

    public static void main_test_train(String[] args) throws IOException, ClassNotFoundException, ClassHierarchyException {
        String fSpecFilePath =
                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/FSpec.spmn";
//                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/FSpec.spmn";

        String serializedGRAAMsFolderPath =
                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/edu.rit.se.design.arcode.fspec2code.Test/SerializedGRAAMs";
//                "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/RMI/Working/50ProjsJar/Train/SerializedGRAAMs";

        String classHierarchyExclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
        String frameworkJarFile = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";

//        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarFile, classHierarchyExclusionFile);

/*        String framework =
                "JAAS";
//                "RMI";

        String frameworkJarPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";
        String frameworkPackage = "javax/security/auth";


        IFDBuilder ifdBuilder =  new IFDBuilder();
        IFD ifd =
//                null;
                ifdBuilder.buildIFD( framework, frameworkJarPath, frameworkPackage );*/

        FSpec fSpec = FSpecUtil.loadFromFile( fSpecFilePath );

        int endNodeCounter = 0;

        for (FSpecNode fSpecNode : StreamSupport.stream(fSpec.spliterator(), false).collect(Collectors.toList())) {
            if( fSpecNode instanceof FSpecEndNode )
                endNodeCounter++;
        }

        List<GRAAM> loadedGRAAMs = GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedGRAAMsFolderPath);

//        runRemoveEdgeExperiment( loadedGRAAMs, fSpec, 10 );
//
//        runRemoveAPIExperiment( loadedGRAAMs, fSpec, 3 );
//
//        runRedundantAPIExperiment( loadedGRAAMs, fSpec, 3 );

//        runSwappedAPIExperiment( loadedGRAAMs, fSpec, 3 );

//        runNextAPISequenceExperiment( loadedGRAAMs, fSpec, 8 );
        runNextAPIExperiment( loadedGRAAMs, fSpec, 8 );


/*        loadedGRAAMs.forEach( graam -> {
//            if(graam.getProjectPath().equals("/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train/kerb-simplekdc-1.0.0-RC2.jar"))
//                System.out.print("");
            List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( graam, fSpec );
            int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
            if( matchedRecommenddationRank != 1 )
                System.out.print("");
            System.out.println( "The found matched recommendation rank is:" + matchedRecommenddationRank );
        } );*/
 /*


        System.out.println(  "Source GRAAM:\n" + new GRAAMVisualizer(graam).dotOutput() + "\n");


        rankedRecommendations.forEach( graphEditDistanceInfo -> {
            System.out.println( "Destination subFSpec with distance " + graphEditDistanceInfo.getDistance() + ":\n" + new FSpecVisualizer( graphEditDistanceInfo.distSubFSpec ).dotOutput() );
//            System.out.println( "Description: \n" + graphEditDistanceInfo.getDescription() );
            System.out.println( "Edited graph:\n " + new FSpecVisualizer( Recommender.applyGraphEdit( graam, fSpec, graphEditDistanceInfo)).dotOutput() );
        } );
*/

/*        K-Percision
        Cummulative graph + random line (x=y)
            Different types of faults : incomplet + faulty*/



    }

    static void runRemoveEdgeExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxNullInjectionNum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxNullInjectionNum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = NullArgumentInjectionTestGenerator.generateTestCases( graam, maxNullInjectionNum );
            generatedTestCases.forEach( (nullInjectionCount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( nullInjectionCount ).put( matchedRecommenddationRank,
                            prevCount + 1 );

                } );

            } );
        } );

        matchedRecommendationRankCounter.forEach( (nullInjectionCount, recommendationRankCountMap) -> {
            if( recommendationRankCountMap == null || recommendationRankCountMap.isEmpty())
                return;
            System.out.println( "Results for " +  + nullInjectionCount + " null injection(s):" );
            int maxRank = StreamSupport.stream( recommendationRankCountMap.keySet().spliterator(), false ).max( (o1, o2) -> o1.compareTo(o2) ).get();
            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + rank );
            System.out.println();

            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + (recommendationRankCountMap.get(rank) == null ? 0 : recommendationRankCountMap.get(rank)) );

            System.out.println();
            System.out.println();
        } );
    }

    static void runRemoveAPIExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxRemoveAPINum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxRemoveAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = MissedAPITestGenerator.generateTestCases( graam, maxRemoveAPINum );
            generatedTestCases.forEach( (nullInjectionCount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( nullInjectionCount ).put( matchedRecommenddationRank,
                            prevCount + 1 );

                } );

            } );
        } );

        matchedRecommendationRankCounter.forEach( (nullInjectionCount, recommendationRankCountMap) -> {
            if( recommendationRankCountMap == null || recommendationRankCountMap.isEmpty())
                return;
            System.out.println( "Results for " +  + nullInjectionCount + " API removal:" );
            int maxRank = StreamSupport.stream( recommendationRankCountMap.keySet().spliterator(), false ).max( (o1, o2) -> o1.compareTo(o2) ).get();
            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + rank );
            System.out.println();

            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + (recommendationRankCountMap.get(rank) == null ? 0 : recommendationRankCountMap.get(rank)) );

            System.out.println();
            System.out.println();
        } );
    }

    static Map<Integer, Map<Integer, Integer>> runNextAPIExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxRemoveLastAPINum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxRemoveLastAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, Map<GRAAM, List<GRAAM>>> generatedTestCases = NextAPITestGenerator.generateTestCases( graam, maxRemoveLastAPINum );
            generatedTestCases.forEach( (lastAPIRemovedCount, testGraamMap) -> {
                testGraamMap.forEach( (testGraamLabel, testGraams)  -> {
                    testGraams.forEach( testGraam -> {
                        List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                        // Here we remove end node of the test label GRAAM to make it possible for investigating whether the returned SubFSpec embeds
                        // this test GRAAM or not. If so, it means that we can use that SubFSpec for Next API recommendation.
//                        GRAAM clonedTestGRAAMLabel = GRAAMBuilder.cloneFromScratch( testGraamLabel ).getKey();
//                        clonedTestGRAAMLabel.removeNodeAndEdges( clonedTestGRAAMLabel.getEndNode() );
                        int matchedRecommenddationRank = Recommender.getEmbededRecommendationRank( testGraamLabel, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                        int prevCount = 0;
                        if( matchedRecommendationRankCounter.get( lastAPIRemovedCount ).get( matchedRecommenddationRank) != null )
                            prevCount = matchedRecommendationRankCounter.get( lastAPIRemovedCount ).get( matchedRecommenddationRank);
                        matchedRecommendationRankCounter.get( lastAPIRemovedCount ).put( matchedRecommenddationRank,
                                prevCount + 1 );


                    } );
                } );

            } );
        } );

        return matchedRecommendationRankCounter;
    }

    static void printExperimentResult(Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter, String experimentTitle, int recPrintCutoff){
        Map<Integer, Integer> aggregatedResult = new HashMap<>();

        matchedRecommendationRankCounter.forEach( (lastAPIRemovedCount, recommendationRankCountMap) -> {
            printOneExperimentResult( recommendationRankCountMap, (experimentTitle + "_" + lastAPIRemovedCount), recPrintCutoff );
            recommendationRankCountMap.forEach( (rank, count) -> {
                if( aggregatedResult.get( rank ) == null )
                    aggregatedResult.put( rank, 0 );
                aggregatedResult.put( rank, aggregatedResult.get(rank) + count );
            } );
        } );

        printOneExperimentResult( aggregatedResult, (experimentTitle + "_Aggregated" ), recPrintCutoff );
    }

    static void printOneExperimentResult(Map<Integer, Integer> rankedRecCount, String expTitle, int maxRank){
        System.out.println( "Results for " + expTitle + ":" );

        int totalRecom = 0;
        for( Integer rank: rankedRecCount.keySet() )
            totalRecom += rankedRecCount.get( rank );

        for( int rank = 0; rank <= maxRank; rank++ )
            System.out.print( "\t" + rank );
        System.out.println();

        for( int rank = 0; rank <= maxRank; rank++ )
            System.out.print( "\t" + (rankedRecCount.get(rank) == null ? 0 : rankedRecCount.get(rank)) );
        System.out.println();

        DecimalFormat df = new DecimalFormat(("#"));

        int cumulativeRecom = 0;
        for( int rank = 0; rank <= maxRank; rank++ ) {
            int rankRec = rankedRecCount.get(rank) == null ? 0 : rankedRecCount.get(rank);
            System.out.print("\t" + df.format( 100.0 * (rankRec + cumulativeRecom) / totalRecom ) );
            if( rank > 0 )
                cumulativeRecom += rankRec;
        }

        System.out.println();

    }


    static void runNextAPISequenceExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxRemoveLastAPINum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxRemoveLastAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = NextAPISequenceTestGenerator.generateTestCases( graam, maxRemoveLastAPINum );
            generatedTestCases.forEach( (lastAPIRemovedCount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( lastAPIRemovedCount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( lastAPIRemovedCount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( lastAPIRemovedCount ).put( matchedRecommenddationRank,
                            prevCount + 1 );

                } );

            } );
        } );

        matchedRecommendationRankCounter.forEach( (lastAPIRemovedCount, recommendationRankCountMap) -> {
            if( recommendationRankCountMap == null || recommendationRankCountMap.isEmpty())
                return;
            System.out.println( "Results for " +  + lastAPIRemovedCount + " Next API Sequence Recommendation:" );
            int maxRank = StreamSupport.stream( recommendationRankCountMap.keySet().spliterator(), false ).max( (o1, o2) -> o1.compareTo(o2) ).get();
            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + rank );
            System.out.println();

            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + (recommendationRankCountMap.get(rank) == null ? 0 : recommendationRankCountMap.get(rank)) );

            System.out.println();
            System.out.println();
        } );
    }

    static void runRedundantAPIExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxRedundantAPINum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxRedundantAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = RedundantAPITestGenerator.generateTestCases( graam, maxRedundantAPINum );
            generatedTestCases.forEach( (redundantAPICount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( redundantAPICount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( redundantAPICount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( redundantAPICount ).put( matchedRecommenddationRank,
                            prevCount + 1 );

                } );

            } );
        } );

        matchedRecommendationRankCounter.forEach( (redundantAPICount, recommendationRankCountMap) -> {
            if( recommendationRankCountMap == null || recommendationRankCountMap.isEmpty())
                return;
            System.out.println( "Results for " +  + redundantAPICount + " API redundant:" );
            int maxRank = StreamSupport.stream( recommendationRankCountMap.keySet().spliterator(), false ).max( (o1, o2) -> o1.compareTo(o2) ).get();
            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + rank );
            System.out.println();

            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + (recommendationRankCountMap.get(rank) == null ? 0 : recommendationRankCountMap.get(rank)) );

            System.out.println();
            System.out.println();
        } );
    }

    static void runSwappedAPIExperiment(List<GRAAM> loadedGRAAMs, FSpec fSpec, int maxSwappedAPINum){
        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxSwappedAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = SwappedAPITestGenerator.generateTestCases( graam, maxSwappedAPINum );
            generatedTestCases.forEach( (redundantAPICount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    /*if( matchedRecommenddationRank != 1 )
                        System.out.print("");
                    System.out.println( "The found matched recommendation rank is:\t" + matchedRecommenddationRank );*/
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( redundantAPICount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( redundantAPICount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( redundantAPICount ).put( matchedRecommenddationRank,
                            prevCount + 1 );

                } );

            } );
        } );

        matchedRecommendationRankCounter.forEach( (redundantAPICount, recommendationRankCountMap) -> {
            if( recommendationRankCountMap == null || recommendationRankCountMap.isEmpty())
                return;
            System.out.println( "Results for " +  + redundantAPICount + " API swapping:" );
            int maxRank = StreamSupport.stream( recommendationRankCountMap.keySet().spliterator(), false ).max( (o1, o2) -> o1.compareTo(o2) ).get();
            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + rank );
            System.out.println();

            for( int rank = 0; rank <= maxRank; rank++ )
                System.out.print( "\t" + (recommendationRankCountMap.get(rank) == null ? 0 : recommendationRankCountMap.get(rank)) );

            System.out.println();
            System.out.println();
        } );
    }
}
