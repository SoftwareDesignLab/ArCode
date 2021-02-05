package edu.rit.se.design.arcode.fspecminer;

//import com.ibm.wala.classLoader.Module;
import edu.rit.se.design.arcode.fspecminer.analysis.ProjectAnalysis;
import edu.rit.se.design.arcode.fspecminer.analysis.ProjectAnalyzer;
import edu.rit.se.design.arcode.fspecminer.analysis.ProjectAnalyzerFromJar;
import edu.rit.se.design.arcode.fspecminer.analysis.ProjectAnalyzerFromSource;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecBuilder;
import edu.rit.se.design.arcode.fspecminer.graam.*;
import edu.rit.se.design.arcode.fspecminer.ifd.IFD;
import edu.rit.se.design.arcode.fspecminer.ifd.IFDBuilder;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
//import fj.Hash;


import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SpecMiner {
    public enum MinerType{FROM_SOURCE, FROM_JAR}

    String framework;
    String projectsFolder;
    String exclusionFile;
    String frameworkJarPath;
    String frameworkPackage;
    MinerType minerType;
    FSpec minedFSpec;
    String serializedGRAAMsFolder;
    String serializedPAGsFolder;
    IFD builtIFD = null;

    public SpecMiner(String framework, String frameworkJarPath, String frameworkPackage, String projectsFolder, String minerType, String exclusionFile ) {
        this.framework = framework;
        this.projectsFolder = projectsFolder;
        this.exclusionFile = exclusionFile;
        this.frameworkJarPath = frameworkJarPath;
        this.frameworkPackage = frameworkPackage;
        this.minerType = MinerType.valueOf( minerType );
        this.serializedGRAAMsFolder = projectsFolder + "/" + "SerializedGRAAMs";
        this.serializedPAGsFolder = projectsFolder + "/" + "SerializedPAUGs";
    }

    public void createGraamsForProjects(IFD ifd, Iterable<String> projects) throws Exception {

        Map<GRAAM, PrimaryAPIUsageGraph> createdGRAAMs = new HashMap<>();
        for( String projectPath: projects)
            createdGRAAMs.putAll( createGraamsForProject( projectPath, ifd ) );

        GRAAMBuilder.saveSerializedGRAAMs( new ArrayList<>(createdGRAAMs.keySet()), serializedGRAAMsFolder);
        PrimaryAPIGraphBuilder.saveSerializedPAGs( new ArrayList<>(createdGRAAMs.values()), serializedPAGsFolder);
    }

    public void mineFrameworkSpecificationFromScratch() throws Exception {
        builtIFD = createIFD();
        createGraamsForProjects( builtIFD, getProjectsPaths() );
        mineFrameworkSpecificationFromSerializedGRAAMs();
    }

    public void mineFrameworkSpecificationFromSerializedGRAAMs() throws Exception {
        List<GRAAM> createdGRAAMs = GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedGRAAMsFolder);
        FSpecBuilder fSpecBuilder = new FSpecBuilder();
        minedFSpec = fSpecBuilder.buildFSpec( framework, createdGRAAMs );
    }

    IFD createIFD() throws IOException {
        IFDBuilder ifdBuilder =  new IFDBuilder();
        IFD ifd = ifdBuilder.buildIFD( framework, frameworkJarPath, frameworkPackage );
//        System.out.println(new IFDVisualizer(ifd).dotOutput());
        return ifd;
    }






/*
    class SimpleServerAnalysis implements ServerAnalysis{
        private Set<String> srcPath;
        private Set<String> libPath;
        private ExecutorService exeService;
        private Future<?> last;

        public SimpleServerAnalysis() {
            exeService = Executors.newSingleThreadExecutor();
        }

        @Override
        public String source() {
            return "Hello World";
        }

        @Override
        public void analyze(Collection<? extends Module> files, MagpieServer server, boolean rerun) {

            if (last != null && !last.isDone()) {
                last.cancel(false);
                if (last.isCancelled())
                    System.out.println("Susscessfully cancelled last analysis and start new");
            }
            Future<?> future = exeService.submit(new Runnable() {
                @Override
                public void run() {
                    setClassPath(server);
                    Collection<AnalysisResult> results = Collections.emptyList();
*//*                    if (srcPath != null) {
                        results = analyze(srcPath, libPath);
                    }
                    server.consume(results, source());*//*
                }
            });
            last = future;
        }



        *//**
         * set up source code path and library path with the project service provided by the server.
         *
         * @param //server
         *//*
        public void setClassPath(MagpieServer server) {
            if (srcPath == null) {
                Optional<IProjectService> opt = server.getProjectService("java");
                if (opt.isPresent()) {
                    JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
                    Set<Path> sourcePath = ps.getSourcePath();
                    if (libPath == null) {
                        libPath = new HashSet<>();
                        ps.getLibraryPath().stream().forEach(path -> libPath.add(path.toString()));
                    }
                    if (!sourcePath.isEmpty()) {
                        Set<String> temp = new HashSet<>();
                        sourcePath.stream().forEach(path -> temp.add(path.toString()));
                        srcPath = temp;
                    }
                }
            }
        }


    }*/

    Map<GRAAM, PrimaryAPIUsageGraph> createGraamsForProject( String projectPath, IFD ifd ) throws Exception {
        CommonConstants.LOGGER.log( Level.INFO, "Creating GRAAM(s) for " + projectPath );
        Map<GRAAM, PrimaryAPIUsageGraph> graamMap = new HashMap<>();

/*        MagpieServer server = new MagpieServer(new ServerConfiguration());
        String language = "java";
        IProjectService javaProjectService = new JavaProjectService();
        server.addProjectService(language, javaProjectService);
        ServerAnalysis myAnalysis = new SimpleServerAnalysis();
        Either<ServerAnalysis, ToolAnalysis> analysis= Either.forLeft(myAnalysis);
        Module module = new SourceDirectoryTreeModule( new File( projectPath ) );
        Set<Module> modules = new HashSet<>();
        modules.add( module );
        myAnalysis.analyze( modules, server, true );

        server.addAnalysis(analysis,language);
        server.launchOnStdio();*/


        ProjectAnalyzer projectAnalyzer = getProjectAnalyzer( projectPath );
        ProjectAnalysis projectAnalysis = projectAnalyzer.analyzeProject();

        List<PrimaryAPIUsageGraph> primaryAPIUsageGraphList = PrimaryAPIGraphBuilder.buildPrimaryAPIUsageGraphs(projectAnalysis, ifd);

        CommonConstants.LOGGER.log( Level.FINE, "\tCreating GRAAM(s) for created PrimaryAPIUsageGraphs" );


        primaryAPIUsageGraphList.forEach( primaryAPIUsageGraph -> {
//            System.out.println("\nPrimary API Usage Graph: ");
//            System.out.println(  new PrimaryAPIUsageGraphVisualizer(primaryAPIUsageGraph).dotOutput() );

            GRAAM graam = GRAAMBuilder.buildGRAAM( primaryAPIUsageGraph, ifd );
//            System.out.println("\nCorresponding GRAAM: ");
//            System.out.println( new GRAAMVisualizer(graam).dotOutput() )    ;
            graamMap.put( graam, primaryAPIUsageGraph );
        });

        CommonConstants.LOGGER.log( Level.FINE, " -> " + graamMap.keySet().size() + " GRAAM(s) was/were created!\n" );

        return graamMap;
    }

    ProjectAnalyzer getProjectAnalyzer( String projectPath ){
        return minerType.equals( MinerType.FROM_SOURCE ) ?
                        new ProjectAnalyzerFromSource(projectPath, exclusionFile, framework) :
                        new ProjectAnalyzerFromJar(projectPath, exclusionFile, framework);
    }

    Iterable<String> getProjectsPaths(){
        List<String> projectPaths = new ArrayList<>();
        File[] files = new File( projectsFolder ).listFiles();
        switch ( minerType ){
            case FROM_JAR:
                Arrays.asList( files ).forEach( file -> {
                    if( file.getName().endsWith( ".jar" ) )
                        projectPaths.add( file.getPath() );
                } );
                break;
            case FROM_SOURCE:
                Arrays.asList( files ).forEach( file -> {
                    if( file.isDirectory() )
                        projectPaths.add( file.getPath() );
                } );
                break;
        }
        return projectPaths;
    }




    public FSpec getMinedFSpec() {
        return minedFSpec;
    }

    public void saveFSpecToFile(String filePath) throws IOException {
        FileOutputStream serializedFSpecFile = null;
        serializedFSpecFile = new FileOutputStream(filePath + "/FSpec.spmn");
        ObjectOutputStream objectOut = new ObjectOutputStream(serializedFSpecFile);
        objectOut.writeObject(minedFSpec);
        objectOut.close();
    }

    public IFD getBuiltIFD() {
        return builtIFD;
    }

    public String getSerializedGRAAMsFolder() {
        return serializedGRAAMsFolder;
    }
}
