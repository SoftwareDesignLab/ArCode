package edu.rit.se.design.arcode.fspecminer.graam.code2vecApi;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CodeToVecHttpClient {
	private String ApiUrl;
	CodeToVecHttpClient(String ApiUrl){
		this.ApiUrl=ApiUrl;
		
	}
	public JSONArray HttpPostRequest(String methodBody) throws IOException {
		HttpPost post = new HttpPost(this.ApiUrl);
	    List<BasicNameValuePair> urlParameters = new ArrayList<>();
	    urlParameters.add(new BasicNameValuePair("code", methodBody));
	
	    post.setEntity(new UrlEncodedFormEntity(urlParameters));
	    try (CloseableHttpClient httpClient = HttpClients.createDefault();
	            CloseableHttpResponse response = httpClient.execute(post)) {
	    			String responseData=EntityUtils.toString(response.getEntity());
	    			return new JSONArray(responseData);
	   }catch(Exception e) {
		   e.printStackTrace();
		   return new JSONArray();
	   }
	}
}
