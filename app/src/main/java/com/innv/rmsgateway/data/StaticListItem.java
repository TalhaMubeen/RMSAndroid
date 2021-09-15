package com.innv.rmsgateway.data;


import android.util.Log;
import org.json.JSONObject;
import java.io.Serializable;
import static android.content.ContentValues.TAG;

public class StaticListItem implements Serializable {
    private String orgCode;
    private String listName;
    private String code;
    private String description;
    private String optParam1;
    private String optParam2;
    private int status = 0;
    private boolean holdCode = false;

    public boolean isHoldCode() {
        return holdCode;
    }

    public void setHoldCode(boolean holdCode) {
        this.holdCode = holdCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public StaticListItem() {
    }

    public StaticListItem(String orgCode, String listName, String code, String description, String optParam1, String optParam2) {
        this.orgCode = orgCode;
        this.listName = listName;
        this.code = code;
        this.description = description;
        this.optParam1 = optParam1;
        this.optParam2 = optParam2;
    }

    public StaticListItem(JSONObject jo) {
        try {
            this.orgCode = jo.getString("tenantId");
            this.listName = jo.getString("listName");
            this.code = jo.getString("code");
            this.description = jo.getString("description");
            this.optParam1 = jo.optString("optParam1");
            if (this.optParam1.equals("")) {
                this.optParam1 = jo.optString("opt1");
            }
            this.optParam2 = jo.optString("optParam2");
            if (this.optParam2.equals("")) {
                this.optParam2 = jo.optString("opt2");
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getCode() {
        return holdCode ? "" : code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOptParam1() {
        return optParam1;
    }

    public void setOptParam1(String optParam1) {
        this.optParam1 = optParam1;
    }

    public String getOptParam2() {
        return optParam2;
    }

    public void setOptParam2(String optParam2) {
        this.optParam2 = optParam2;
    }

    public JSONObject getJSONObject() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("code", getCode());
            jo.put("desc", getDescription());
            if (optParam1 != null)
                jo.put("opt1", optParam1);
            if (optParam2 != null)
                jo.put("opt2", optParam2);
            return jo;
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject getMultiJSONObject() {
        JSONObject jo = new JSONObject();
        try {
            //jo.put("ltype", 0);
            jo.put("listname", getListName());
            jo.put("code", getCode());
            if (!getDescription().equals(""))
                jo.put("desc", getDescription());
            if (optParam1 != null)
                jo.put("optParam1", optParam1);
            if (optParam2 != null)
                jo.put("optParam2", optParam2);
            return jo;
        } catch (Exception e) {
            return null;
        }
    }
}
