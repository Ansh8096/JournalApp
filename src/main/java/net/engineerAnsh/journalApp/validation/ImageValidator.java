package net.engineerAnsh.journalApp.validation;

import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
public class ImageValidator {

    private static final long MAX_FILE_SIZE = (long) 5 * 1024 * 1024; // 5 MB
    private static final int MAX_IMAGES = 5;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );


    private void validateNotEmpty(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Please select an image.");
        }

    }

    private void validateSize(MultipartFile image) {

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Image size must not exceed 5 MB.");
        }

    }

    private void validateContentType(MultipartFile image) {

        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Only JPG, PNG and WEBP images are allowed."
            );
        }
    }

    private void validateImageCount(
            List<MultipartFile> images
    ) {

        if (images.size() > MAX_IMAGES) {

            throw new BadRequestException(
                    "A journal can contain at most "
                            + MAX_IMAGES
                            + " images."
            );
        }
    }

    public void validate(MultipartFile image) {
        validateNotEmpty(image);
        validateSize(image);
        validateContentType(image);
    }

    public void validate(List<MultipartFile> images) {

        if (images == null || images.isEmpty()) {
            return;
        }


        validateImageCount(images);

        for (MultipartFile image : images) {
            validate(image);
        }
    }

}