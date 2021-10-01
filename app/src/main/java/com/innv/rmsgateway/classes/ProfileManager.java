package com.innv.rmsgateway.classes;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    public static Profile DefaultProfile = new Profile("Default",25.5,-87,50);
    public static Profile FreezerProfile = new Profile("Freezer",0,-90,80);

    private static List<Profile> profileList = new ArrayList<>();

    public static void init(){
        profileList.add(DefaultProfile);
        profileList.add(FreezerProfile);
    }

    public static Profile getProfile(String name){
        for(Profile profile: profileList){
            if(profile.getName().equals(name)){
                return  profile;
            }
        }
        return null;
    }

    public static List<String> getAllProfilesName(){
        List<String> retList = new ArrayList<>();
        for(Profile profile: profileList){
            retList.add(profile.getName());
        }
        return  retList;
    }


}
