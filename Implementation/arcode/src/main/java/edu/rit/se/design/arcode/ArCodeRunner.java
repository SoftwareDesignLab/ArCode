package edu.rit.se.design.arcode;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.rit.se.design.arcode.fspec2recom.FSpec2Recom;
import edu.rit.se.design.arcode.fspec2recom.FSpec2RecomExperimentRunner;
import edu.rit.se.design.arcode.fspecminer.SpecMiner;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
import org.apache.commons.cli.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ArCodeRunner {
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
        trainProjsSpecMiner.saveFSpecToFile( fspecOutputPath );

        CommonConstants.LOGGER.log( Level.INFO, "Analyzing testing projects");
        SpecMiner testProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, testProjectsPath, minerType, exclusionFilePath);
        testProjsSpecMiner.mineFrameworkSpecificationFromScratch(false);


        FSpec2Recom.fspec2Recom( trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), 10 );
    }

}
