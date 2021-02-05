package edu.rit.se.design.specminer.analysis.dependencyresolver;

import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public interface ProjectDependencyResolver {
    public List<String> findDependencies(String projectFolderPath) throws Throwable;
}
