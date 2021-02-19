package edu.rit.se.design.arcode;

import edu.rit.se.design.arcode.fspec2recom.*;
import edu.rit.se.design.arcode.fspecminer.SpecMiner;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ArCodeRunnerICSA21 {
    static Map<String, String> extractProgramArguments(String[] args) throws ParseException {
        Map<String, String> programArguments = new HashMap<>();
        Option framework = Option.builder( "framework" ).required(true).hasArg().build();
        Option trainProjectsPath = Option.builder( "trainProjectsPath" ).hasArg().required(true).build();
        Option testProjectsPath = Option.builder( "testProjectsPath" ).hasArg().required(true).build();
        Option fspecOutputPath = Option.builder( "fspecOutputPath" ).hasArg().required(false).build();
        Option exclusionFilePath = Option.builder( "exclusionFilePath" ).hasArg().required(true).build();
        Option frameworkJarPath = Option.builder( "frameworkJarPath" ).hasArg().required(true).build();
        Option frameworkPackage = Option.builder( "frameworkPackage" ).hasArg().required(true).build();
        Option mode = Option.builder( "mode" ).hasArg().required(false).build();

        Options options = new Options();

        options.addOption( framework );
        options.addOption( trainProjectsPath );
        options.addOption( testProjectsPath );
        options.addOption( fspecOutputPath );
        options.addOption( exclusionFilePath );
        options.addOption( frameworkJarPath );
        options.addOption( frameworkPackage );
        options.addOption( mode );

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine =  parser.parse(options, args);
        programArguments.put( "framework", commandLine.getOptionValue( "framework" ) );
        programArguments.put( "trainProjectsPath", commandLine.getOptionValue( "trainProjectsPath" ) );
        programArguments.put( "testProjectsPath", commandLine.getOptionValue( "testProjectsPath" ) );
        programArguments.put( "minerType", "FROM_JAR" );
        programArguments.put( "fspecOutputPath", commandLine.getOptionValue( "fspecOutputPath" ) != null ? commandLine.getOptionValue( "fspecOutputPath" ) : commandLine.getOptionValue( "trainProjectsPath" ) );
        programArguments.put( "exclusionFilePath", commandLine.getOptionValue( "exclusionFilePath" ) );
        programArguments.put( "frameworkJarPath", commandLine.getOptionValue( "frameworkJarPath" ) );
        programArguments.put( "frameworkPackage", commandLine.getOptionValue( "frameworkPackage" ) );
        programArguments.put( "mode", commandLine.getOptionValue( "mode" ) );

        return programArguments;
    }

    public static void main(String[] args) throws Exception {

        CommonConstants.LOGGER.setLevel( Level.FINE );

        Map<String, String> programArguments = extractProgramArguments( args );

        String framework = programArguments.get("framework");

        String trainProjectsPath = programArguments.get("trainProjectsPath");
        String testProjectsPath = programArguments.get("testProjectsPath");
        String minerType = programArguments.get("minerType");
        String fspecOutputPath = programArguments.get("fspecOutputPath");
        String exclusionFilePath = programArguments.get("exclusionFilePath");
        String frameworkJarPath = programArguments.get("frameworkJarPath");
        String frameworkPackage = programArguments.get("frameworkPackage");
        String mode = programArguments.get("mode");

        SpecMiner trainProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, trainProjectsPath, minerType, exclusionFilePath);
        CommonConstants.LOGGER.log( Level.INFO, "Analyzing training projects");

        trainProjsSpecMiner.mineFrameworkSpecificationFromScratch(true);
//        trainProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();
        trainProjsSpecMiner.saveFSpecToFile( fspecOutputPath );

        CommonConstants.LOGGER.log( Level.INFO, "Analyzing testing projects");
        SpecMiner testProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, testProjectsPath, minerType, exclusionFilePath);
        testProjsSpecMiner.mineFrameworkSpecificationFromScratch(false);
//        testProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();

        int recPrintCutoff = 15;

//        FSpec2Recom.fspec2Recom( trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 10 );
        nextAPIRecommendationExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), recPrintCutoff);

        missedAPIExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 3, recPrintCutoff);

        swappedAPIExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 6, recPrintCutoff);

    }

    public static void mainOrig(String[] args) throws Exception {

        CommonConstants.LOGGER.setLevel( Level.FINE );

        Map<String, String> programArguments = extractProgramArguments( args );

        String framework = programArguments.get("framework");

        String trainProjectsPath = programArguments.get("trainProjectsPath");
        String testProjectsPath = programArguments.get("testProjectsPath");
        String minerType = programArguments.get("minerType");
        String fspecOutputPath = programArguments.get("fspecOutputPath");
        String exclusionFilePath = programArguments.get("exclusionFilePath");
        String frameworkJarPath = programArguments.get("frameworkJarPath");
        String frameworkPackage = programArguments.get("frameworkPackage");
        String mode = programArguments.get("mode");

        SpecMiner trainProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, trainProjectsPath, minerType, exclusionFilePath);
        CommonConstants.LOGGER.log( Level.INFO, "Analyzing training projects");

//        trainProjsSpecMiner.mineFrameworkSpecificationFromScratch(true);
        trainProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();
        trainProjsSpecMiner.saveFSpecToFile( fspecOutputPath );

        CommonConstants.LOGGER.log( Level.INFO, "Analyzing testing projects");
        SpecMiner testProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, testProjectsPath, minerType, exclusionFilePath);
//        testProjsSpecMiner.mineFrameworkSpecificationFromScratch(false);
        testProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();

        int recPrintCutoff = 15;

//        FSpec2Recom.fspec2Recom( trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 10 );
        nextAPIRecommendationExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), recPrintCutoff);

        missedAPIExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 3, recPrintCutoff);

        swappedAPIExperiment(trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 6, recPrintCutoff);

    }

    static void swappedAPIExperiment(FSpec fSpec, String serializedTestGRAAMsFolderPath, int maxSwappedAPINum, int recPrintCutoff) throws IOException, ClassNotFoundException {
        CommonConstants.LOGGER.log( Level.INFO, "Performing Experiment: Swapped API Recommendation");

        List<GRAAM> loadedTestGRAAMs =  GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTestGRAAMsFolderPath);

        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxSwappedAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedTestGRAAMs.forEach( graam -> {
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

        printExperimentResult( matchedRecommendationRankCounter, "Swapped API Recommendation", recPrintCutoff );

    }

    static void missedAPIExperiment(FSpec fSpec, String serializedTestGRAAMsFolderPath, int maxRemoveAPINum, int recPrintCutoff) throws IOException, ClassNotFoundException {

        CommonConstants.LOGGER.log( Level.INFO, "Performing Experiment: Missed API Recommendation");

        List<GRAAM> loadedTestGRAAMs =  GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTestGRAAMsFolderPath);

        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();
        for (int i = 0; i < maxRemoveAPINum; i++){
            matchedRecommendationRankCounter.put(i, new HashMap<>());
        }

        loadedTestGRAAMs.forEach( graam -> {
            Map<Integer, List<GRAAM>> generatedTestCases = MissedAPITestGenerator.generateTestCases( graam, maxRemoveAPINum );
            generatedTestCases.forEach( (nullInjectionCount, testGraams) -> {
                testGraams.forEach( testGraam -> {
                    List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, fSpec );
                    int matchedRecommenddationRank = Recommender.getMatchedRecommendationRank( graam, rankedRecommendations );
                    int prevCount = 0;
                    if( matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank) != null )
                        prevCount = matchedRecommendationRankCounter.get( nullInjectionCount ).get( matchedRecommenddationRank);
                    matchedRecommendationRankCounter.get( nullInjectionCount ).put( matchedRecommenddationRank,
                            prevCount + 1 );
                } );
            } );
        } );

        printExperimentResult( matchedRecommendationRankCounter, "Missed API Recommendation", recPrintCutoff );
    }

    static void nextAPIRecommendationExperiment( FSpec trainProjectsFSpec, String serializedTestGRAAMsFolderPath, int recPrintCutoff ) throws IOException, ClassNotFoundException {

        CommonConstants.LOGGER.log( Level.INFO, "Performing Experiment: Next API Recommendation");

        List<GRAAM> loadedTestGRAAMs =  GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTestGRAAMsFolderPath);

        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounter = new HashMap<>();

        Map<Integer, Map<Integer, Integer>> matchedRecommendationRankCounterRes =
                runNextAPIExperiment( loadedTestGRAAMs, trainProjectsFSpec, 8 );
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

        printExperimentResult( matchedRecommendationRankCounter, "Next API Recommendation", recPrintCutoff );
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
//            printOneExperimentResult( recommendationRankCountMap, (experimentTitle + "_" + lastAPIRemovedCount), recPrintCutoff );
            recommendationRankCountMap.forEach( (rank, count) -> {
                if( aggregatedResult.get( rank ) == null )
                    aggregatedResult.put( rank, 0 );
                aggregatedResult.put( rank, aggregatedResult.get(rank) + count );
            } );
        } );

        printOneExperimentResult( aggregatedResult, ( experimentTitle ), recPrintCutoff );
    }

    static void printOneExperimentResult(Map<Integer, Integer> rankedRecCount, String expTitle, int maxRank){
        System.out.println( "\nResults for " + expTitle + ":" );

        int totalRecom = 0;
        for( Integer rank: rankedRecCount.keySet() )
            totalRecom += rankedRecCount.get( rank );

        System.out.print( "Top-K Recommendations\t" );
        for( int rank = 1; rank <= maxRank; rank++ )
            System.out.print( "\t" + rank );
        System.out.println();

/*        System.out.print( "Percentage of correct Recommendations\t" );
        for( int rank = 0; rank <= maxRank; rank++ )
            System.out.print( "\t" + (rankedRecCount.get(rank) == null ? 0 : rankedRecCount.get(rank)) );
        System.out.println();*/

        DecimalFormat df = new DecimalFormat(("#"));

        System.out.print( "Top-K Accuracy (%)   \t" );
        int cumulativeRecom = 0;
        for( int rank = 1; rank <= maxRank; rank++ ) {
            int rankRec = rankedRecCount.get(rank) == null ? 0 : rankedRecCount.get(rank);
            System.out.print("\t" + df.format( 100.0 * (rankRec + cumulativeRecom) / totalRecom ) );
            if( rank > 0 )
                cumulativeRecom += rankRec;
        }

        System.out.println();

    }


}
