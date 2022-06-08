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

import java.util.HashMap;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FrameworkUtilsFactory {
    private static HashMap<String, FrameworkUtils> frameworkUtilsMap = new HashMap<>();
    public static boolean CONSIDER_OVERLOADED_METHODS_AS_THE_SAME = false;
    public static FrameworkUtils getFrameworkUtils( String framework ) throws FrameworkUtilityNotFoundException {
        FrameworkUtils frameworkUtils =  frameworkUtilsMap.get( framework );
        if( frameworkUtils != null )
            return frameworkUtils;
        switch( framework ){
            case "JAAS":
                frameworkUtils = new JAASUtils(CONSIDER_OVERLOADED_METHODS_AS_THE_SAME);
                break;
            case "RMI":
                frameworkUtils = new RMIUtils(CONSIDER_OVERLOADED_METHODS_AS_THE_SAME);
                break;
            case "SPRING":
                frameworkUtils = new SpringUtils(CONSIDER_OVERLOADED_METHODS_AS_THE_SAME);
                break;
            default:
                throw new FrameworkUtilityNotFoundException( "No framework utility class is implemented for " + framework + " yet!" );
        }
        frameworkUtilsMap.put( framework, frameworkUtils );
        return frameworkUtils;
    }


}
