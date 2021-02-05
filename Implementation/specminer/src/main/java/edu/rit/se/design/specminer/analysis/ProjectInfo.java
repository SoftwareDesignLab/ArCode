package edu.rit.se.design.specminer.analysis;

import java.io.Serializable;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ProjectInfo implements Serializable {
    String path;

    public ProjectInfo(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
