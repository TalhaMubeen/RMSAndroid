package com.innv.rmsgateway.classes;

import java.util.ArrayList;
import java.util.List;

public class DefrostProfileManager {

    public static DefrostProfile None = new DefrostProfile("None",0,0,0,0);
    public static DefrostProfile defrostInterval1 = new DefrostProfile("Defrost 11/12",11,0,12,0);
    public static DefrostProfile defrostInterval2 = new DefrostProfile("Defrost 13/14",13,0,14,0);
    public static DefrostProfile defrostInterval3 = new DefrostProfile("Defrost 15:30/16:30",15,30,16,30);


    private static List<DefrostProfile> defrostProfiles = new ArrayList<>();

    public static void init(){
        defrostProfiles.clear();
        defrostProfiles.add(None);
        defrostProfiles.add(defrostInterval1);
        defrostProfiles.add(defrostInterval2);
        defrostProfiles.add(defrostInterval3);
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
