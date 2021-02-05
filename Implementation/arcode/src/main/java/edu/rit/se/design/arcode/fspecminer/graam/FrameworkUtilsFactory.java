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
