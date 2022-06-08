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

package edu.rit.se.design.arcode.fspecminer.ifd;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class IFD extends DirectedGraph<MethodRepresentation, IFDEdgeType, IFDEdgeInfo> {
    String framework;
    static IFDEdgeInfo defaultIFDEdgeInfo = new IFDEdgeInfo();

    public IFD(String framework/*, Map<PrimaryAPIUsageGraphEdgeType, Map<String, Set<String>>> apiDependencies*/) {
        this.framework = framework;
//        this.apiDependencies = apiDependencies;
    }

    public String getFramework() {
        return framework;
    }

    @Override
    protected IFDEdgeType getDefaultEdgeType() {
        return IFDEdgeType.FIELD_BASE_DEPENDENCY;
    }

    @Override
    protected IFDEdgeInfo getDefaultEdgeInfo(IFDEdgeType edgeType) {
        return defaultIFDEdgeInfo;
    }

    @Override
    public String getTitle() {
        return "IFD model of " + framework;
    }

    /*    Map<PrimaryAPIUsageGraphEdgeType, Map<String, Set<String>>> apiDependencies = new HashMap<>();
    public IFD(String framework) {
        this.framework = framework;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void addAPIDependency( String fromAPI, String toAPI, PrimaryAPIUsageGraphEdgeType apiDependencyType ){
        if( !apiDependencies.containsKey( apiDependencyType ) )
            apiDependencies.put( apiDependencyType, new HashMap<>() );
        if( !apiDependencies.get( apiDependencyType ).containsKey( fromAPI ) )
            apiDependencies.get( apiDependencyType ).put( fromAPI, new HashSet<>());
        apiDependencies.get( apiDependencyType ).get( fromAPI ).add( toAPI );
    }

    public boolean hasDependency( String fromAPI, String toAPI, PrimaryAPIUsageGraphEdgeType apiDependencyType ){
        return apiDependencies.get( apiDependencyType ) != null &&
                apiDependencies.get( apiDependencyType ).get( fromAPI ) != null &&
                apiDependencies.get( apiDependencyType ).get( fromAPI ).contains( toAPI );
    }

    public StringBuilder toDotGraph(boolean showSelfLoops){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( "digraph prof {\n" +
                "graph [label=\"" + framework + " IFD\"];" +
                "\tratio = fill;\n" +
                "\tnode [style=filled];\n" );

        apiDependencies.keySet().forEach( apiDependencyType -> {
            apiDependencies.get( apiDependencyType ).forEach((fromAPI, toAPIs) -> {
                toAPIs.forEach( toAPI -> {
                    String label = "\"" + fromAPI + "\" -> \"" + toAPI + "\"";
                    label = label.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","");
                    stringBuilder.append( label + ";\n"  );
                });
            });
        } );

        stringBuilder.append("}");

        return stringBuilder;
    }*/
}
