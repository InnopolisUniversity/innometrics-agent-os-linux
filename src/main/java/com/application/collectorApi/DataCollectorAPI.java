package com.application.collectorApi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class DataCollectorAPI {

    private final String activityPostUrl, processPostUrl;
    private final String processGetUrl,activityGetUrl;
    private String token = "";

    public DataCollectorAPI(){
        //this.activityPostUrl = "http://10.90.138.244:9091/V1/activity";
        //this.processPostUrl = "http://10.90.138.244:9091/V1/process";
        this.activityPostUrl = "https://innometric.guru:9091/V1/activity"; //production
        this.processPostUrl = "https://innometric.guru:9091/V1/process"; //production
        this.processGetUrl = "";
        this.activityGetUrl = "";

    }
    public DataCollectorAPI(String activityPostUrl,String processPostUrl,String token){
        this.activityPostUrl = activityPostUrl;
        this.processPostUrl = processPostUrl;
        this.processGetUrl = "";
        this.activityGetUrl = "";
        this.token = token;
    }
    public DataCollectorAPI(final String token){
        this();
        this.token = token;
    }

    public int post(JSONArray data, final String dataType) {
        if(data.isEmpty()) {
            return 0;
        }
        String Posturl = dataType.equals("activities") ?  activityPostUrl : processPostUrl;
        JSONObject postBody = new JSONObject();

        postBody.put(dataType,data);

        HttpClient processesReportClient = HttpClient.newBuilder().build();

        HttpRequest processesReportrequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("accept", "application/json")
                .header("Token", token)
                .uri(URI.create(Posturl))
                .POST(HttpRequest.BodyPublishers.ofString(postBody.toString()))
                .build();

        try {
            HttpResponse<?> response = processesReportClient.send(processesReportrequest, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (Exception ex) {
            //System.out.println("POST Failed!!");
            return -1;
        }
    }

    public Object get(final String dataType) {
        String Getturl = dataType.equals("activities") ?  activityGetUrl : processGetUrl;
        return null;
    }

    public void setToken(final String token){
        this.token = token;
    }
}
