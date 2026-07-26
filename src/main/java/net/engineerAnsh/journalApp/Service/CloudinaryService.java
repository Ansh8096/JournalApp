package net.engineerAnsh.journalApp.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Config.cloudinary.CloudinaryProperties;
import net.engineerAnsh.journalApp.Dto.user.ImageUploadResponse;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import net.engineerAnsh.journalApp.model.JournalImage;
import net.engineerAnsh.journalApp.validation.ImageValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final ImageValidator imageValidator;
    private final CloudinaryProperties cloudinaryProperties;

    private ImageUploadResponse uploadImage(
            MultipartFile image,
            String folder
    ) {

        try {

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.asMap(
                            "folder",
                            folder
                    )
            );

            return ImageUploadResponse.builder()
                    .imageUrl(uploadResult.get("secure_url").toString())
                    .publicId(uploadResult.get("public_id").toString())
                    .build();

        } catch (IOException e) {
            throw new BadRequestException(
                    "Failed to upload image, reason: " + e.getMessage()
            );
        }
    }

    public ImageUploadResponse uploadProfileImage(
            MultipartFile image
    ) {

        imageValidator.validate(image);

        return uploadImage(
                image,
                cloudinaryProperties.getProfileImageFolder()
        );
    }

    public List<JournalImage> uploadJournalImages(
            List<MultipartFile> images
    ) {

        imageValidator.validate(images);

        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<JournalImage> uploadedImages = new ArrayList<>();

        try {

            for (MultipartFile image : images) {

                uploadedImages.add(
                        uploadJournalImage(image)
                );
            }

            return uploadedImages;

        } catch (Exception ex) {

            deleteJournalImages(uploadedImages);

            throw ex;
        }
    }

    public void deleteImage(String publicId) {

        try {

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

        } catch (IOException e) {
            throw new BadRequestException("Failed to delete image, reason: " + e.getMessage());
        }
    }

    public void deleteJournalImages(
            List<JournalImage> images
    ) {

        if (images == null || images.isEmpty()) {
            return;
        }

        for (JournalImage image : images) {

            try {

                deleteImage(image.getPublicId());

            } catch (Exception ex) {

                log.warn(
                        "Failed to delete journal image: {}",
                        image.getPublicId(),
                        ex
                );
            }
        }
    }

    public JournalImage uploadJournalImage(
            MultipartFile image
    ) {

        // Validate before uploading
        imageValidator.validate(image);

        try {

            Map<?, ?> uploadResult =
                    cloudinary.uploader().upload(
                            image.getBytes(),
                            ObjectUtils.asMap(
                                    "folder",
                                    cloudinaryProperties.getJournalImageFolder()
                            )
                    );

            return JournalImage.builder()
                    .imageUrl(uploadResult.get("secure_url").toString())
                    .publicId(uploadResult.get("public_id").toString())
                    .build();

        } catch (IOException ex) {

            throw new BadRequestException(
                    "Failed to upload image, reason: "
                            + ex.getMessage()
            );
        }
    }
}