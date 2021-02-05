package edu.rit.se.design.arcode.fspecminer;

import edu.rit.se.design.arcode.fspecminer.fspec.FSpecVisualizer;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SpecMinerRunner {
/*    public static void main(String[] args) throws Exception {
        String framework = "JAAS";
//        String sourceProj = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsSrc/Train/JaasInterProceduralTest";
//        String sourceProj = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/testProj";
        String sourceProj = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsSrc/Train/JaasInterProceduralTest";
//        String sourceProj = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsSrc/Train/JaasInterProceduralTest/JAASTestClass.java";
        String jarProj = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train/JaasInterProceduralTest.jar";

        String exclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/config/JAASJavaExclusions.txt";
//        String inclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/config/JAASJavaInclusions.txt";

        String frameworkJarPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";
        String frameworkPackage =
        "javax/security/auth";
//                "java/rmi";
//        "org/junit";
//                "org/apache/jackrabbit";
//        "org/apache/lucene";

        IFDBuilder ifdBuilder =  new IFDBuilder();
        IFD ifd = ifdBuilder.buildIFD( framework, frameworkJarPath, frameworkPackage );
        System.out.println(new IFDVisualizer(ifd).dotOutput());

        ProjectAnalyzer projectAnalyzer =
                new ProjectAnalyzerFromSource(sourceProj, exclusionFile, framework);
//                new ProjectAnalyzerFromJar(jarProj, exclusionFile, framework);
        ProjectAnalysis projectAnalysis = projectAnalyzer.analyzeProject();

        List<PrimaryAPIUsageGraph> primaryAPIUsageGraphList = PrimaryAPIGraphBuilder.buildPrimaryAPIUsageGraphs(projectAnalysis, ifd);

        List<GRAAM> graamList = new ArrayList<>();

        primaryAPIUsageGraphList.forEach( primaryAPIUsageGraph -> {
            System.out.println("\nPrimary API Usage Graph: ");
            System.out.println(  new PrimaryAPIUsageGraphVisualizer(primaryAPIUsageGraph).dotOutput() );
            GRAAM graam = GRAAMBuilder.buildGRAAM( primaryAPIUsageGraph, ifd );
            System.out.println("\nCorresponding GRAAM: ");
            System.out.println( new GRAAMVisualizer(graam).dotOutput() )    ;
            graamList.add( graam );
        });

//        graamList.add( graamList.get(0) );

        FSpecBuilder fSpecBuilder = new FSpecBuilder();
        FSpec fSpec = fSpecBuilder.buildFSpec( framework, graamList );
        System.out.println( new FSpecVisualizer( fSpec ).dotOutput());
    }*/

    static Map<String, String> extractProgramArguments(String[] args) throws ParseException {
        Map<String, String> programArguments = new HashMap<>();
        Option framework = Option.builder( "framework" ).required(true).hasArg().build();
        Option projectsPath = Option.builder( "projectsPath" ).hasArg().required(true).build();
        Option minerType = Option.builder( "minerType" ).hasArg().required(false).build();
        Option fspecOutputPath = Option.builder( "fspecOutputPath" ).hasArg().required(true).build();
        Option exclusionFilePath = Option.builder( "exclusionFilePath" ).hasArg().required(true).build();
        Option frameworkJarPath = Option.builder( "frameworkJarPath" ).hasArg().required(true).build();
        Option frameworkPackage = Option.builder( "frameworkPackage" ).hasArg().required(true).build();

        Options options = new Options();

        options.addOption( framework );
        options.addOption( projectsPath );
        options.addOption( minerType );
        options.addOption( fspecOutputPath );
        options.addOption( exclusionFilePath );
        options.addOption( frameworkJarPath );
        options.addOption( frameworkPackage );

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine =  parser.parse(options, args);
        programArguments.put( "framework", commandLine.getOptionValue( "framework" ) );
        programArguments.put( "projectsPath", commandLine.getOptionValue( "projectsPath" ) );
        programArguments.put( "minerType", commandLine.getOptionValue( "minerType" ) != null ? commandLine.getOptionValue( "minerType" ) : "FROM_JAR" );
        programArguments.put( "fspecOutputPath", commandLine.getOptionValue( "fspecOutputPath" ) );
        programArguments.put( "exclusionFilePath", commandLine.getOptionValue( "exclusionFilePath" ) );
        programArguments.put( "frameworkJarPath", commandLine.getOptionValue( "frameworkJarPath" ) );
        programArguments.put( "frameworkPackage", commandLine.getOptionValue( "frameworkPackage" ) );



        return programArguments;
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> programArguments = extractProgramArguments( args );

        String framework = programArguments.get("framework"); // "JAAS";

        String projectPath = programArguments.get("projectsPath"); // null;
        String minerType = programArguments.get("minerType"); //null;
        String fspecOutputPath = programArguments.get("fspecOutputPath"); //"/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working";

/*
        projectPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsSrc/Train";
        minerType = "FROM_SOURCE";
*/

//        projectPath = "/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Data/JAAS/Working/50ProjsJar/Train";
//        minerType = "FROM_JAR";

        String exclusionFilePath = programArguments.get("exclusionFilePath"); //"/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/specminer/config/JAASJavaExclusions.txt";
    //        String inclusionFile = "/Users/ali/Academic/RIT/Research/Projects/NewSpecMiner/Implementation/config/JAASJavaInclusions.txt";

        String frameworkJarPath = programArguments.get("frameworkJarPath"); //"/Users/ali/Academic/RIT/Research/Projects/SpecMiner/Implementation/SpecMiner/config/JREs/jre1.8.0_131/lib/rt.jar";
        String frameworkPackage = programArguments.get("frameworkPackage"); //
//                "javax/security/auth";
    //                "java/rmi";
    //        "org/junit";
    //                "org/apache/jackrabbit";
    //        "org/apache/lucene";

        SpecMiner specMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, projectPath, minerType, exclusionFilePath);
        specMiner.mineFrameworkSpecificationFromScratch();
        System.out.println( "Final FSpec:\n" + new FSpecVisualizer( specMiner.getMinedFSpec() ).dotOutput() + "\n");

        specMiner.saveFSpecToFile( fspecOutputPath );

    }
}

