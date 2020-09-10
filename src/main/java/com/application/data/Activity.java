package com.application.data;
import com.application.model.Model;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class Activity {
    private int activityID = -1;
    private String activityType = "";
    private String browser_title = "";
    private String browser_url = "";
    private String end_time = "";
    private String executable_name = "";
    private String idle_activity= "ss";
    private String ip_address= "";
    private String mac_address= "";
    private String osversion= "";
    private String pid= "";
    private String start_time= "";
    private String userID= "123";

    public Activity(){}
    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("activityID",activityID);
        result.put("activityType",activityType);
        result.put("browser_title",browser_title);
        result.put("browser_url",browser_url);
        result.put("end_time",end_time);
        result.put("idle_activity",idle_activity);
        result.put("ip_address",ip_address);
        result.put("mac_address",mac_address);
        result.put("osversion",osversion);
        result.put("pid",pid);
        result.put("start_time",start_time);
        result.put("userID",userID);
        result.put("executable_name",executable_name);

        return result;
    }
    public void setActivityValues(Model m, HashMap <String,String>values){
        //System.out.println("Setting activity values");
        this.mac_address = Model.currentMAC;
        this.ip_address = Model.currentIP;
        this.osversion = Model.currentOS;
        this.userID = m.getLoginUsername();
        this.pid = values.getOrDefault("pid","none");
        this.browser_title = values.getOrDefault("browser_title","none");
        this.executable_name = values.getOrDefault("executable_name","none");
        this.activityType = values.getOrDefault("activityType","none");
        this.start_time = values.getOrDefault("start_time","00:00:00");
        String procStateCode = values.getOrDefault("idle_activity","Ss");
        this.idle_activity = isIdle(procStateCode);
    }
    public void setEndTime(String time){ this.end_time = time;}
    private String isIdle(String code){ return Model.IdleStates.contains(code) ? "true":"false"; }
}
