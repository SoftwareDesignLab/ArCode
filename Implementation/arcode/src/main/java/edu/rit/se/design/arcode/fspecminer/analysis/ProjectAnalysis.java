package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class ProjectAnalysis {
    CallGraph callGraph;
    PointerAnalysis<InstanceKey> pointerAnalysis;
    ProjectInfo projectInfo;

    public ProjectAnalysis(ProjectInfo projectInfo, CallGraph callGraph, PointerAnalysis<InstanceKey> pointerAnalysis) {
        this.callGraph = callGraph;
        this.pointerAnalysis = pointerAnalysis;
        this.projectInfo = projectInfo;
    }

    public CallGraph getCallGraph() {
        return callGraph;
    }

    public PointerAnalysis<InstanceKey> getPointerAnalysis() {
        return pointerAnalysis;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
}
