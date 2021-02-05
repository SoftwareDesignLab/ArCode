package edu.rit.se.design.specminer.analysis;

import com.ibm.wala.util.config.SetOfClasses;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SetOfExclusionClasses extends SetOfClasses {

    List<String> exclusions;

    public SetOfExclusionClasses( String exclusionPath ) throws IOException {
        exclusions = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader( new FileReader( exclusionPath ));
        String line = bufferedReader.readLine();
        while( line != null ){
            exclusions.add( line );
            line = bufferedReader.readLine();
        }
    }

    public boolean contains(String s) {
        for( String exclusion: exclusions )
            if( s.matches( exclusion ) )
                return true;
        return false;
    }

    public void add(String s) {
        System.out.print("");
    }
}
