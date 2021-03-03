/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
