package com.application.model;

import com.application.data.Activity;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.HashMap;

public class BrowserEventsHandler implements HttpHandler {
    protected Model m;

    public BrowserEventsHandler(Model m) {
        this.m = m;
    }

    protected void sendError(HttpExchange httpExchange) throws IOException {
        var outputStream = httpExchange.getResponseBody();
        var resp = "NO JSON PROVIDED";
        httpExchange.sendResponseHeaders(400, resp.length());
        outputStream.write(resp.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if (!"POST".equals(httpExchange.getRequestMethod())) {
            this.sendError(httpExchange);
            return;
        }

        var body_stream = httpExchange.getRequestBody();
        var body_json = new String(body_stream.readAllBytes());
        try {
            var body = new JSONArray(body_json);
            System.out.println(body.toString());
            // TODO: send the request to the server

            for (var i = 0; i < body.length(); ++i) {
                var obj = (JSONObject) body.get(i);


                var hashMap = new HashMap<String, String>();
                hashMap.put("start_time", (String) obj.get("start_time"));
                hashMap.put("activityType", "browser");
                hashMap.put("browser_title", (String) obj.get("browser_title"));
                hashMap.put("browser_url", (String) obj.get("browser_url"));

                Activity currentActivity = new Activity();
                currentActivity.setActivityValues(m, hashMap);
                currentActivity.setEndTime((String) obj.get("end_time"));
//                m.setAddActivity(currentActivity);
                m.activitiesQueue.add(currentActivity);
            }

            var outputStream = httpExchange.getResponseBody();
            var resp = "OK";
            httpExchange.sendResponseHeaders(200, resp.length());
            outputStream.write(resp.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (JSONException e) {
            this.sendError(httpExchange);
        }
    }
}
