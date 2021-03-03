package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.util.config.SetOfClasses;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SetOfInclusionClasses extends SetOfClasses {

    List<String> inclusions;

    public SetOfInclusionClasses(String inclusions ) throws IOException {
        this.inclusions = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader( new FileReader( inclusions ));
        String line = bufferedReader.readLine();
        while( line != null ){
            this.inclusions.add( line );
            line = bufferedReader.readLine();
        }
    }

    public boolean contains(String s) {
        for( String inclusion: inclusions)
            if( s.matches( inclusion ) )
                return false;
        return true;
    }

    public void add(String s) {
        System.out.print("");
    }
}
