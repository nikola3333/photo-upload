package com.example.awsimageupload.profile;

import com.example.awsimageupload.bucket.BucketName;
import com.example.awsimageupload.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.apache.http.entity.ContentType.*;

@Service
public class UserProfileService {
    private final UserProfileDataAccessService userProfileDataAccessService;

    private final FileStore fileStore;

    @Autowired
    public UserProfileService(UserProfileDataAccessService userProfileDataAccessService, FileStore fileStore) {
        this.userProfileDataAccessService = userProfileDataAccessService;
        this.fileStore = fileStore;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfileDataAccessService.getUserProfiles();
    }


    void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {

        // 1. check if image is empty
        isFileEmpty(file);

        // 2. if file is an image
        isImage(file);

        // 3. the user exists in our database
        UserProfile user = getUserProfileOrThrow(userProfileId);

        // 4. grab some metadata from file if any
        Map<String, String> metadata = extractMetadata(file);

        // 5. store the image in s3 and update database (userProfileImageLink) with s3 image link
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        String fileName = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());

        try {
            fileStore.save(path, fileName, Optional.of(metadata), file.getInputStream());
            user.setUserProfileImageLink(fileName);
        } catch (IOException e){
            throw new IllegalStateException(e);
        }

    }

    public byte[] downloadUserProfileImage(UUID userProfileId) {

        UserProfile user = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
        return user.getUserProfileImageLink().map(key -> fileStore.download(path, key)).orElse(new byte[0]);

    }


    private Map<String, String> extractMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();

        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    private UserProfile getUserProfileOrThrow(UUID userProfileId) {
        return userProfileDataAccessService
                .getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User profile %s not found", userProfileId)));
    }

    private void isImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType(), IMAGE_GIF.getMimeType()).contains(file.getContentType())){
            throw new IllegalStateException("file must be an image");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if(file.isEmpty()){
            throw new IllegalStateException("cannot upload empty file [ " + file.getSize() + " ]");
        }
    }


}
