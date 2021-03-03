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

package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.SpecMiner;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpec;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMBuilder;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMVisualizer;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpec2Recom {

    public static Map<GRAAM, List<GraphEditDistanceInfo>> fspec2Recom( FSpec trainProjectsFSpec, String serializedTestGRAAMsFolderPath, int rankingCutoff ) throws IOException, ClassNotFoundException {

        Map<GRAAM, List<GraphEditDistanceInfo>> recommendationMap = new HashMap<>();

        CommonConstants.LOGGER.log( Level.INFO, "Finding API misuses and recommendations for GRAAMs in folder \"" + serializedTestGRAAMsFolderPath + "\"...");
        CommonConstants.LOGGER.log( Level.INFO, "Recommendation cutoff: " + rankingCutoff);

        List<GRAAM> loadedTestGRAAMs =  GRAAMBuilder.loadGRAAMsFromSerializedFolder(serializedTestGRAAMsFolderPath);


        loadedTestGRAAMs.forEach( testGraam -> {
            System.out.println( "***********************************************************************" );
            System.out.println( "GRAAM title: " + testGraam.getTitle() + ""  );
            System.out.println( "GRAAM dot graph: \n" + new GRAAMVisualizer( testGraam ).dotOutput() + "\n" );

            List<GraphEditDistanceInfo> rankedRecommendations = fspec2Recom( trainProjectsFSpec, testGraam, rankingCutoff );

            recommendationMap.put( testGraam, rankedRecommendations );

            if ( rankedRecommendations.get(0).getDistance() == 0 ) {
                System.out.println("This GRAAM follows a correct API usage. No recommendation needed.");
                System.out.println( "Recommendation #" + 0 + ":\n" +
                        new SubFSpecVisualizer( rankedRecommendations.get(0).distSubFSpec ).dotOutput() );
                return;
            }

            System.out.println("This GRAAM does not follow a correct API usage. Please find recommendations as below:");
            for ( int i = 0; i < rankedRecommendations.size(); i++ )
                System.out.println( "Recommendation #" + (i + 1) + ":\n" +
                new SubFSpecVisualizer( rankedRecommendations.get(i).distSubFSpec ).dotOutput() );
        } );

        return recommendationMap;
    }

    public static List<GraphEditDistanceInfo> fspec2Recom( FSpec trainProjectsFSpec, GRAAM testGraam, int rankingCutoff ){
        List<GraphEditDistanceInfo> rankedRecommendations = Recommender.findRankedRecommendations( testGraam, trainProjectsFSpec );
        return rankedRecommendations.subList( 0, Math.min( rankingCutoff, rankedRecommendations.size() ) );
    }

}
