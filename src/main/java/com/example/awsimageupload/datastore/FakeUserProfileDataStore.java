package com.example.awsimageupload.datastore;

import com.example.awsimageupload.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FakeUserProfileDataStore {

    private static final List<UserProfile> USER_PROFILES = new ArrayList<>();

    static {
        USER_PROFILES.add(new UserProfile(UUID.fromString("167619b5-4000-4de9-99d4-3c3da00e1ef5"), "janetjones", null));
        USER_PROFILES.add(new UserProfile(UUID.fromString("a06fc905-85c8-46b9-86a4-b8a76cab21ef"), "antoniojunior", null));
    }

    public List<UserProfile> getUserProfiles(){
        return USER_PROFILES;
    }

}
