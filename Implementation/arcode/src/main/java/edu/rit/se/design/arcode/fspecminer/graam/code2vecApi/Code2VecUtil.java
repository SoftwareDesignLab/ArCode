package edu.rit.se.design.arcode.fspecminer.graam.code2vecApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class Code2VecUtil {
    static String CODE_2_VEC_REST_SERVER = "http://localhost:5555";

    static CodeToVecHttpClient code2vecClientInstance = null;

    static CodeToVecHttpClient getCode2VecClientInstance(){
        if( code2vecClientInstance == null )
        /**
         * Initialize httpClient object that talk with code2vec python code
         */
            code2vecClientInstance = new CodeToVecHttpClient(CODE_2_VEC_REST_SERVER);

        return code2vecClientInstance;
    }

    public static Map<String,Double> getRankedMethodNameMap( String method ){
        //method = method.replace("\"", "");
        Map<String,Double> rankedMethodNameMap = new LinkedHashMap<String,Double>();
        try {
            JSONArray jsonArray = getCode2VecClientInstance().HttpPostRequest(method);
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject entry=jsonArray.getJSONObject(i);
                rankedMethodNameMap.put(getMethodName(entry), getProbability(entry));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return  rankedMethodNameMap;

    }

    static Double getProbability(JSONObject entry) {
        Double probability=Double.parseDouble(entry.get("probability").toString());
        return probability;
    }
    static String getMethodName(JSONObject entry) {
        JSONArray entryNames=(JSONArray) entry.get("name");
        String name=entryNames.getString(0);
        for(int j=1;j<entryNames.length();j++) {
            String str=entryNames.getString(j);
            str=str.substring(0, 1).toUpperCase() + str.substring(1);
            name+=str;

        }
        return name;
    }

}
