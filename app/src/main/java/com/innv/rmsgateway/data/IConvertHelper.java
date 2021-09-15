package com.innv.rmsgateway.data;

import org.json.JSONObject;

public interface IConvertHelper {
    boolean parseJsonObject(JSONObject jsonObject);
    JSONObject getJsonObject();

}
