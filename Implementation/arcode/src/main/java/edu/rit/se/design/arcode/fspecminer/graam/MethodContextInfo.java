/*
 * Copyright (c) 2022 - Present. Rochester Institute of Technology
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

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class MethodContextInfo implements Serializable {
    static final int ST_SCORE_NAME_RECOMMENDATION_THRESHOLD = 10;
    Map<String,Double> scoredRecommendedNames;

    public MethodContextInfo(Map<String,Double> scoredRecommendedNames){
        this.scoredRecommendedNames = scoredRecommendedNames;
    }
    public Map<String,Double> getScoredRecommendedNames(){
        return scoredRecommendedNames;
    }
    public MethodContextInfo clone(){
        return new MethodContextInfo( new HashMap<>( scoredRecommendedNames ) );
    }
    public void merge( MethodContextInfo other ){
        if( other == null )
            return;

        other.getScoredRecommendedNames().forEach( (name, otherScore) -> {
            // If the name does not already exist, just add it to the map, otherwise, add the max of the score of
            // other's score and this one's score.
            double score = getScoredRecommendedNames().get(name) == null ? 0 : other.getScoredRecommendedNames().get(name);
            getScoredRecommendedNames().put( name, Math.max( score, otherScore ) );
        } );

        if( getScoredRecommendedNames().size() <= ST_SCORE_NAME_RECOMMENDATION_THRESHOLD )
            return;
        List<Double> sortedScores = getScoredRecommendedNames().values().stream().sorted(Comparator.reverseOrder()).limit( ST_SCORE_NAME_RECOMMENDATION_THRESHOLD ).collect(Collectors.toList());
        List<String> toBeRemovedKeys = getScoredRecommendedNames().keySet().stream().filter( name -> !sortedScores.contains(  getScoredRecommendedNames().get( name ) ) ).collect(Collectors.toList());
        toBeRemovedKeys.forEach(toBeRemovedKey -> getScoredRecommendedNames().remove( toBeRemovedKey ) );
    }

}
