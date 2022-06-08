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

package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.analysis.ProjectInfo;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class PrimaryAPIUsageGraph extends DirectedGraph<DirectedGraphNode, PrimaryAPIUsageGraphEdgeType, PrimaryAPIUsageGraphEdgeInfo> {
    ProjectInfo projectInfo;
    PrimaryAPIUsageGraphEdgeType defaultLabel;
    static PrimaryAPIUsageGraphEdgeInfo primaryAPIUsageGraphEdgeInfo = new PrimaryAPIUsageGraphEdgeInfo();
    public PrimaryAPIUsageGraph(ProjectInfo projectInfo, PrimaryAPIUsageGraphEdgeType defaultLabel) {
        this.defaultLabel = defaultLabel;
        this.projectInfo = projectInfo;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }


    @Override
    protected PrimaryAPIUsageGraphEdgeType getDefaultEdgeType() {
        return defaultLabel;
    }

    @Override
    protected PrimaryAPIUsageGraphEdgeInfo getDefaultEdgeInfo(PrimaryAPIUsageGraphEdgeType edgeType) {
        return primaryAPIUsageGraphEdgeInfo;
    }

    @Override
    public String getTitle() {
        String[] splitedPath = getProjectInfo().getPath().split( Pattern.quote(File.separator) );
        return splitedPath[ splitedPath.length - 1 ] ;
    }
}
