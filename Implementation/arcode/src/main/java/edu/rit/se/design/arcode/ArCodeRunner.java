package edu.rit.se.design.arcode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.CodeGenerationException;
import edu.rit.se.design.arcode.fspec2recom.*;
import edu.rit.se.design.arcode.fspecminer.SpecMiner;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMVisualizer;
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
        Option mineTrainFromScratch = Option.builder( "mineTrainFromScratch" ).hasArg().required(false).build();
        Option mineTestFromScratch = Option.builder( "mineTestFromScratch" ).hasArg().required(false).build();
        Option recommendationCutOff = Option.builder( "recommendationCutOff" ).hasArg().required(false).build();


        Options options = new Options();

        options.addOption( framework );
        options.addOption( trainProjectsPath );
        options.addOption( testProjectsPath );
        options.addOption( fspecOutputPath );
        options.addOption( exclusionFilePath );
        options.addOption( frameworkJarPath );
        options.addOption( frameworkPackage );
        options.addOption( mineTrainFromScratch );
        options.addOption( mineTestFromScratch );
        options.addOption( recommendationCutOff );
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
        programArguments.put( "mineTrainFromScratch", commandLine.getOptionValue( "mineTrainFromScratch" ) );
        programArguments.put( "mineTestFromScratch", commandLine.getOptionValue( "mineTestFromScratch" ) );
        programArguments.put( "recommendationCutOff", commandLine.getOptionValue( "recommendationCutOff" ) );

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
        int recommendationCutOff = programArguments.get("recommendationCutOff") != null ? Integer.parseInt( programArguments.get("recommendationCutOff") ) : 10;
        boolean mineTrainFromScratch = programArguments.get("mineTrainFromScratch") != null ? Boolean.parseBoolean( programArguments.get("mineTrainFromScratch") ) : true;
        boolean mineTestFromScratch = programArguments.get("mineTestFromScratch") != null ? Boolean.parseBoolean( programArguments.get("mineTestFromScratch") ) : true;

        SpecMiner trainProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, trainProjectsPath, minerType, exclusionFilePath);
        CommonConstants.LOGGER.log( Level.INFO, "Analyzing training projects");

        if( mineTrainFromScratch )
            trainProjsSpecMiner.mineFrameworkSpecificationFromScratch(true);
        else
            trainProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();

        trainProjsSpecMiner.saveFSpecToFile( fspecOutputPath );

        CommonConstants.LOGGER.log( Level.INFO, "Analyzing testing projects");
        SpecMiner testProjsSpecMiner = new SpecMiner(framework, frameworkJarPath, frameworkPackage, testProjectsPath, minerType, exclusionFilePath);

        if( mineTestFromScratch )
            testProjsSpecMiner.mineFrameworkSpecificationFromScratch(false);
        else
            testProjsSpecMiner.mineFrameworkSpecificationFromSerializedGRAAMs();

        Map<GRAAM, List<GraphEditDistanceInfo>> recommendationMap = FSpec2Recom.fspec2Recom( trainProjsSpecMiner.getMinedFSpec(), testProjsSpecMiner.getSerializedGRAAMsFolder(), recommendationCutOff );
        generateDotAndCodeSnippetFiles( recommendationMap, testProjectsPath, frameworkJarPath, exclusionFilePath );

    }
    static void generateDotAndCodeSnippetFiles( Map<GRAAM, List<GraphEditDistanceInfo>> recommendationMap, String testProjectsPath, String frameworkJarPath, String exclusionFilePath ) throws ClassHierarchyException, IOException {
        ClassHierarchyUtil classHierarchyUtil = new ClassHierarchyUtil(frameworkJarPath, exclusionFilePath);
        String recommendationsFolder = testProjectsPath + File.separator + "Recommendations";

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        File recommendationsDirectory = new File( recommendationsFolder );
        if( !recommendationsDirectory.exists() )
            recommendationsDirectory.mkdir();
        recommendationMap.keySet().forEach( graam -> {
            List<GraphEditDistanceInfo> graphEditDistanceInfos = recommendationMap.get( graam );

            int maxDistance = graphEditDistanceInfos.stream().max( (o1, o2) -> o1.getDistance() - o2.getDistance() ).get().getDistance();

            String recommendationFolder = recommendationsFolder + File.separator + graam.getTitle();
            File recommendationDirectory = new File( recommendationFolder );
            if( !recommendationDirectory.exists() )
                recommendationDirectory.mkdir();

            String graamFolder = recommendationFolder + File.separator + "GRAAM" ;
            File graamDirectory = new File( graamFolder );
            if( !graamDirectory.exists() )
                graamDirectory.mkdir();

            try {
                StringBuilder graamCodeSnippet = new StringBuilder(CodeGeneratorUtil.GRAAM2Code(graam, "Automatically generated code", "currentImplementation", classHierarchyUtil) );
                createFile( graamFolder + File.separator + "code", graamCodeSnippet );
                StringBuilder graamDot = new GRAAMVisualizer( graam ).dotOutput();
                createFile( graamFolder + File.separator + "dot", graamDot );
            } catch (CodeGenerationException | IOException e) {
                e.printStackTrace();
            }

            for(  int i = 0; i < graphEditDistanceInfos.size(); i++ ){
                GraphEditDistanceInfo graphEditDistanceInfo = graphEditDistanceInfos.get(i);
                String recomFolder = recommendationFolder + File.separator + "RECOM_" + (i+1) ;
                File recomDirectory = new File( recomFolder );
                if( !recomDirectory.exists() )
                    recomDirectory.mkdir();

                try {
                    StringBuilder recomCodeSnippet = new StringBuilder(CodeGeneratorUtil.SubFSpec2Code(graphEditDistanceInfo.getDistSubFSpec(), "Automatically generated code", "recommendedImplementation", classHierarchyUtil) );
                    createFile( recomFolder + File.separator + "code", recomCodeSnippet );
                    StringBuilder recomDot = new SubFSpecVisualizer( graphEditDistanceInfo.getDistSubFSpec() ).dotOutput();
                    createFile( recomFolder + File.separator + "dot", recomDot );
                    int distance = graphEditDistanceInfo.getDistance();
                    double score = (1.0 * (maxDistance + 1 - distance)) / maxDistance;
                    createFile( recomFolder + File.separator + "score", new StringBuilder( decimalFormat.format( score ) ) );

                } catch (CodeGenerationException | IOException e) {
                    e.printStackTrace();
                }
            }
        } );
    }

    static void createFile( String filePath, StringBuilder content ) throws IOException {
        File newFile = new File(filePath);
        newFile.createNewFile();
        FileWriter myWriter = new FileWriter(newFile);
        myWriter.write(content.toString());
        myWriter.close();
    }

}
