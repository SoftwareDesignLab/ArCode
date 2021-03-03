package edu.rit.se.design.arcode.fspecminer.analysis;

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
