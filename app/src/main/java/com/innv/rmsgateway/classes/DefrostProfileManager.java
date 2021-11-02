package com.innv.rmsgateway.classes;

import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DefrostProfileManager {

    private static List<DefrostProfile> defrostProfiles = new ArrayList<>();

    public static void init(){

        defrostProfiles.clear();
        List<StaticListItem> profiles =  NodeDataManager.getAllDefrostProfilesList();

        for(StaticListItem item : profiles){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(item.getOptParam1());
                DefrostProfile prof = new DefrostProfile();
                if(prof.parseJsonObject(jsonObject)) {
                    defrostProfiles.add(prof);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    public static List<DefrostProfile> getDefrostProfiles(){ return defrostProfiles;}

    public static DefrostProfile getDefrostProfile(String name){
        for(DefrostProfile profile: defrostProfiles){
            if(profile.getName().equals(name)){
                return  profile;
            }
        }
        return null;
    }

    public static List<String> getDefrostProfileNames(){
        List<String> retList = new ArrayList<>();
        for(DefrostProfile profile: defrostProfiles){
            retList.add(profile.getName());
        }
        return  retList;
    }
}
