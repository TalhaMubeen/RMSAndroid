package com.innv.rmsgateway.classes;

import com.innv.rmsgateway.data.IConvertHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class User implements IConvertHelper {
    private String id ="";
    private String name="";
    private String email="";
    private Boolean isLogging;
    private Boolean isRemoved;
    private Boolean isAdmin;
    private Boolean active;
    private String groupId;
    private String phoneNumber;
    private String mobileNumber;
    private String _id;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    private boolean changeOnly=false;
    private HashMap<String, Object> hmBackupValues;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isChangeOnly() {
        return changeOnly;
    }

    public void setChangeOnly(boolean changeOnly) {
        this.changeOnly = changeOnly;
    }

    public Boolean getRemoved() {
        return isRemoved;
    }

    public void setRemoved(Boolean removed) {
        isRemoved = removed;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getLogging() {
        return isLogging;
    }

    public void setLogging(Boolean logging) {
        isLogging = logging;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(JSONObject jsonObject){
        parseJsonObject(jsonObject);
    }
    public User(){ }

    @Override
    public boolean parseJsonObject(JSONObject jsonObject) {
        try {
            hmBackupValues= Globals.getHashMapJSONObject(jsonObject);

            setId(jsonObject.optString("id"));
            setName(jsonObject.optString("name",""));
            setEmail(jsonObject.optString("email",""));
            setActive(jsonObject.optBoolean("active", true));
            setRemoved(jsonObject.optBoolean("isRemoved",false));
            setAdmin(jsonObject.optBoolean("isAdmin", false));
            setGroupId(jsonObject.optString("group_id",""));
            setPhoneNumber(jsonObject.optString("phone", ""));
            setMobileNumber(jsonObject.optString("mobile", ""));
            set_id(jsonObject.optString("_id", ""));
            hmBackupValues.put("id",getId());
            setPassword(jsonObject.optString("password", ""));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public JSONObject getJsonObject() {
        JSONObject jo =new JSONObject();
        if(changeOnly){
            jo=getJsonObjectChanged();
            if(jo.length()==0){
                return null;
            }else
            {
                return jo;
            }
        }
        try {
            jo.put("id",getId());
            jo.put("name",getName());
            jo.put("email",getEmail());
            jo.put("isRemoved", getRemoved());
            jo.put("isAdmin", getAdmin());
            jo.put("active", getActive());
            jo.put("group_id", getGroupId());
            jo.put("phone", getPhoneNumber());
            jo.put("mobile", getMobileNumber());
            jo.put("_id", get_id());
            jo.put("password", getPassword());

        }catch (Exception e){
            e.printStackTrace();
        }
        return jo;
    }
    private JSONObject getJsonObjectChanged(){
        JSONObject jo=new JSONObject();
        try {
            putJSONProperty(jo,"id",getId());
            putJSONProperty(jo,"name",getName());
            putJSONProperty(jo,"email",getEmail());
            putJSONProperty(jo,"isRemoved", getRemoved());
            putJSONProperty(jo,"isAdmin", getAdmin());
            putJSONProperty(jo,"active", getActive());
            putJSONProperty(jo, "group_id", getGroupId());
            putJSONProperty(jo, "phone", getPhoneNumber());
            putJSONProperty(jo, "mobile", getMobileNumber());
            putJSONProperty(jo,"_id", get_id());
            putJSONProperty(jo,"password", getPassword());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jo;
    }
    private boolean putJSONProperty(JSONObject jo, String fieldName, Object value){
        Object oldValue=null;
        oldValue=hmBackupValues.get(fieldName);
        if(oldValue ==null || (oldValue !=null && !oldValue.equals(value))){
            try {
                jo.put(fieldName,value);
            } catch (JSONException e) {
                e.printStackTrace();
                return  false;
            }
        }
        return true;
    }

}