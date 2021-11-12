package com.innv.rmsgateway.classes;

import com.innv.rmsgateway.data.NodeDataManager;
import com.innv.rmsgateway.data.StaticListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    public static Profile IceCream = new Profile( "Ice Cream",-10,-1,-87,30,50, 5, true);
    public static Profile FrozenFood = new Profile("Frozen Food",-10, 0,-90,50,80, 5, true);
    public static Profile FruitVegDrinksDairy = new Profile( "Fruits, Veg, Cold Drinks & Dairy",1, 15,-90,50,80, 5, true);
    public static Profile WalkinChiller = new Profile("Walk-in Chiller", 10, 25,-90,50,80, 5, true);

    private static List<Profile> profileList = new ArrayList<>();

    public static void init(){
        profileList.clear();
        List<StaticListItem> profiles =  NodeDataManager.getAllProfilesList();
        if(profiles.size() == 0){ //Adding default profiles in-to db
            NodeDataManager.AddorUpdateProfile(IceCream.getTitle(), IceCream, false);
            NodeDataManager.AddorUpdateProfile(FrozenFood.getTitle(), FrozenFood, false);
            NodeDataManager.AddorUpdateProfile(FruitVegDrinksDairy.getTitle(), FruitVegDrinksDairy, false);
            NodeDataManager.AddorUpdateProfile(WalkinChiller.getTitle(), WalkinChiller, false);

            profiles =  NodeDataManager.getAllProfilesList();
        }

        for(StaticListItem item : profiles){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(item.getOptParam1());
                Profile prof = new Profile();
                if(prof.parseJsonObject(jsonObject)) {
                    profileList.add(prof);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public static List<Profile> getProfileList(){
        return profileList;
    }

    public static Profile getProfile(String title){
        for(Profile profile: profileList){
            if(profile.getTitle().equals(title)){
                return new Profile(profile);
            }
        }
        return null;
    }

    public static List<String> getProfilesTitle(){
        List<String> retList = new ArrayList<>();
        for(Profile profile: profileList){
            retList.add(profile.getTitle());
        }
        return  retList;
    }

}
