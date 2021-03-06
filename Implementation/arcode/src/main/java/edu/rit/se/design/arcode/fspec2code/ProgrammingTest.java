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

package edu.rit.se.design.arcode.fspec2code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class ProgrammingTest {
    public static void main(String []args){
        String input = "acb";
        System.out.println( "creating permutation..." );

        String sortedInput = sort( input );

        List<String> permutations = createPermutations( sortedInput );
        System.out.println( "permutation qty: " + permutations.size() );
        for( String permutation: permutations )
            System.out.println( permutation );
    }
    static List<String> createPermutations( String str ){
        List<String> permutations = new ArrayList<>();
        if( str.length() == 1 ){
            permutations.add( str );
            return permutations;
        }
        for( int i = 0; i < str.length(); i++ ){
            String newString = str.substring(0, i) + str.substring(i+1);
            String removedStr = str.substring(i, i+1);
            List<String> subPermutations = createPermutations( newString );
            for( String subPermutation: subPermutations )
                permutations.add( removedStr + subPermutation );
        }
        return permutations;
    }
    static String sort( String input ){
        List<String> strings = new ArrayList<>();
        for( int i = 0; i < input.length(); i++ )
            strings.add( input.substring(i, i+1) );
        Collections.sort( strings );
        input = "";
        for( String str: strings )
            input += str;
        return input;
    }
}
