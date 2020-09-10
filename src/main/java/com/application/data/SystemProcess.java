package com.application.data;

import com.application.model.Model;
import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class SystemProcess {
    private String collectedTime = "";
    private String ip_address = "";
    private String mac_address = "";
    private JSONArray measurementReportList ;
    private String osversion = "";
    private String pid = "";
    private String processName = "";
    private String userID;

    public SystemProcess(){
        measurementReportList = new JSONArray();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("collectedTime",this.collectedTime);
        result.put("ip_address",this.ip_address);
        result.put("mac_address",this.mac_address);

        for(int i=0; i<this.measurementReportList.size(); i++){
            JSONObject tempObj = (JSONObject) this.measurementReportList.get(i);
            Iterator<String> keys = tempObj.keySet().iterator();
            while(keys.hasNext()){
                String key = keys.next();
                result.put(key, tempObj.getOrDefault(key,"none"));
            }
        }

        result.put("osversion",this.osversion);
        result.put("pid",this.pid);
        result.put("processName",this.processName);
        result.put("userID",this.userID);

        return result;
    }

    public void setProcessValues(Model m, Map <String, JSONObject>measurements,String collectedTime, String pid, String procName) {

        this.mac_address = Model.currentMAC;
        this.ip_address = Model.currentIP;
        this.osversion = Model.currentOS;
        this.userID = m.getLoginUsername();
        this.collectedTime = collectedTime;
        this.pid = pid;
        this.processName = procName;

        //set Measurement Report List
        for (Map.Entry<String, JSONObject> entry : measurements.entrySet()){
            String key = entry.getKey();
            JSONObject value = entry.getValue();

            JSONObject tempObj = new JSONObject();
            tempObj.put("alternativeLabel"+key,key);
            tempObj.put("capturedDate"+key,this.collectedTime);
            tempObj.put("measurementTypeId"+key,value.getOrDefault("measurementTypeId","None"));
            tempObj.put("value"+key,value.getOrDefault("value","0.0"));

            this.measurementReportList.add(tempObj);
        }
    }
}
