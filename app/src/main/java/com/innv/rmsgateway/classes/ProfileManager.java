package com.innv.rmsgateway.classes;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    public static Profile IceCream = new Profile("IceCream", "Ice Cream",-5,-1,-87,30,50);
    public static Profile FrozenFood = new Profile("FrozenFood", "Frozen Food",-10, 0,-90,50,80);
    public static Profile FruitVegDrinksDairy = new Profile("FruitVegDrinksDairy", "Fruits, Veg, Cold Drinks & Dairy",5, 10,-90,50,80);
    public static Profile WalkinChiller = new Profile("WalkinChiller","Walk-in Chiller", 15, 25,-90,50,80);

    private static List<Profile> profileList = new ArrayList<>();

    public static void init(){
      //  profileList.add(Default);
        profileList.clear();
        profileList.add(IceCream);
        profileList.add(FrozenFood);
        profileList.add(FruitVegDrinksDairy);
        profileList.add(WalkinChiller);
    }

    public static List<Profile> getProfileList(){
        return profileList;
    }

    public static Profile getProfile(String title){
        for(Profile profile: profileList){
            if(profile.getTitle().equals(title)){
                return  profile;
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
