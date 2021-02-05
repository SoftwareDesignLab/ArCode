package edu.rit.se.design.specminer.analysis.dependencyresolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class ProjectDependencyResolverFactory {
    static Map<String, ProjectDependencyResolver> projectDependencyResolverInstances = new HashMap<>();
    public static ProjectDependencyResolver getProjectDependencyResolver( String projectFolderPath ){
/*        if( !projectDependencyResolverInstances.containsKey( "MAVEN_BASED" ) )
            projectDependencyResolverInstances.put( "MAVEN_BASED", new MavenProjectDependencyResolver() );
        return projectDependencyResolverInstances.get( "MAVEN_BASED" );*/

        if( !projectDependencyResolverInstances.containsKey( "MAGPIE" ) )
            projectDependencyResolverInstances.put( "MAGPIE", new MagpieBridgeProjectDependencyResolver() );
        return projectDependencyResolverInstances.get( "MAGPIE" );
    }
}
