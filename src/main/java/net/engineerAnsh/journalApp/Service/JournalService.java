package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.journals.*;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalRepository;
import net.engineerAnsh.journalApp.criteria.JournalSearchCriteria;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import net.engineerAnsh.journalApp.mapper.JournalMapper;
import net.engineerAnsh.journalApp.model.JournalImage;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserService userService;
    private final JournalMapper journalMapper;
    private final CloudinaryService cloudinaryService;
    private static final int MAX_IMAGES_PER_JOURNAL = 20;

    private String getLoggedInUser() {
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // 'SecurityContextHolder.getContext().getAuthentication() '-> gives the current logged-in user...
        return userAuthenticated.getName();
    }

    private List<Journal> getJournals(JournalSearchCriteria criteria, User user) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        Stream<Journal> stream = user.getJournals()
                .stream()
                .sorted(Comparator.comparing(Journal::getCreatedAt).reversed());

        // Search by title or content
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {

            String query = criteria.getQuery().trim().toLowerCase();

            stream = stream.filter(journal ->
                    journal.getTitle().toLowerCase().contains(query)
                            || journal.getContent().toLowerCase().contains(query));
        }

        // Filter by favorite
        if (criteria.getFavorite() != null) {

            stream = stream.filter(journal ->
                    journal.isFavorite() == criteria.getFavorite());
        }

        // Filter by mood
        if (criteria.getMood() != null) {

            stream = stream.filter(journal ->
                    journal.getMood() == criteria.getMood());
        }

        // Filter by start date
        if (criteria.getFrom() != null) {

            stream = stream.filter(journal ->
                    !journal.getCreatedAt()
                            .toLocalDate()
                            .isBefore(criteria.getFrom()));
        }

        // Filter by end date
        if (criteria.getTo() != null) {

            stream = stream.filter(journal ->
                    !journal.getCreatedAt()
                            .toLocalDate()
                            .isAfter(criteria.getTo()));
        }

        return stream.toList();
    }

    private User getAuthenticatedUser() {

        String username = getLoggedInUser();

        User user = userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        return user;
    }

    private Journal findUserJournal(
            User user,
            ObjectId journalId
    ) {

        return user.getJournals()
                .stream()
                .filter(journal -> journal.getId().equals(journalId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Journal not found.")
                );
    }

    private JournalImage findJournalImage(
            Journal journal,
            String publicId
    ) {

        if (publicId == null || publicId.isBlank()) {
            throw new BadRequestException("Invalid image identifier.");
        }

        return journal.getImages()
                .stream()
                .filter(image -> image.getPublicId().equals(publicId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Journal image not found."
                        )
                );
    }

    private String determineCoverImageUrl(
            List<JournalImage> images
    ) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.get(0).getImageUrl(); // or get(0) depending on your Java version
    }

    private void assignDefaultCoverIfMissing(
            Journal journal,
            List<JournalImage> uploadedImages
    ) {
        if (journal.getCoverImageUrl() == null &&
                !uploadedImages.isEmpty()) {
            journal.setCoverImageUrl(
                    uploadedImages.get(0).getImageUrl()
            );
        }
    }

    private void validateJournalImageLimit(
            Journal journal,
            List<MultipartFile> images
    ) {

        if (images == null || images.isEmpty()) {
            return;
        }

        int existingImages = journal.getImages().size();
        int newImages = images.size();

        if (existingImages + newImages > MAX_IMAGES_PER_JOURNAL) {

            throw new BadRequestException(
                    "A journal can contain at most "
                            + MAX_IMAGES_PER_JOURNAL
                            + " images."
            );
        }
    }

    @Transactional
    public JournalResponseDto createJournal(
            CreateJournalRequestDto request,
            List<MultipartFile> images
    ) {

        // Get the currently authenticated user
        String username = getLoggedInUser();

        User user = userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        // Upload journal images (returns an empty list if no images were provided)
        List<JournalImage> journalImages = cloudinaryService.uploadJournalImages(images);

        String coverImageUrl = determineCoverImageUrl(journalImages);

        try {

            // Build the journal
            Journal journal = Journal.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .mood(request.getMood())
                    .coverImageUrl(coverImageUrl)
                    .images(journalImages)
                    .build();

            // Save the journal
            Journal savedJournal = journalRepository.save(journal);

            // Associate the journal with the current user
            user.getJournals().add(savedJournal);

            userService.saveEntry(user);

            // Return response
            return journalMapper.toResponseDto(savedJournal);

        } catch (Exception ex) {

            // Best-effort cleanup of uploaded Cloudinary images.
            // Database operations are rolled back by @Transactional,
            // but Cloudinary uploads are not.
            cloudinaryService.deleteJournalImages(journalImages);

            throw ex;
        }
    }

    @Transactional
    public void deleteJournal(ObjectId journalId) {

        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        // Delete associated Cloudinary images (best effort)
        cloudinaryService.deleteJournalImages(journal.getImages());

        // Remove the journal reference from the user
        user.getJournals().remove(journal);

        // Delete the journal document
        journalRepository.delete(journal);

        // Persist the updated user
        userService.saveEntry(user);
    }

    public JournalResponseDto getJournalById(
            ObjectId journalId
    ) {
        User user = getAuthenticatedUser();
        Journal journal = findUserJournal(user, journalId);
        return journalMapper.toResponseDto(journal);
    }

    public JournalResponseDto updateJournalById(
            ObjectId journalId,
            UpdateJournalRequestDto request
    ) {

        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        if (request.getTitle() != null &&
                !request.getTitle().isBlank()) {

            journal.setTitle(request.getTitle());
        }

        if (request.getContent() != null &&
                !request.getContent().isBlank()) {

            journal.setContent(request.getContent());
        }

        if (request.getMood() != null) {
            journal.setMood(request.getMood());
        }

        Journal updatedJournal = journalRepository.save(journal);

        return journalMapper.toResponseDto(updatedJournal);
    }

    @Transactional
    public JournalResponseDto uploadJournalImages(
            ObjectId journalId,
            List<MultipartFile> images
    ) {

        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        // Validate total image count
        validateJournalImageLimit(journal, images);

        // Upload new images
        List<JournalImage> uploadedImages = cloudinaryService.uploadJournalImages(images);

        try {

            // Append the uploaded images
            journal.getImages().addAll(uploadedImages);

            // Assign a cover image if it doesn't already exist...
            assignDefaultCoverIfMissing(journal, uploadedImages);

            // Persist the updated journal
            Journal updatedJournal = journalRepository.save(journal);

            return journalMapper.toResponseDto(updatedJournal);

        } catch (Exception ex) {

            // Database failed after upload.
            // Cleanup uploaded Cloudinary images.
            cloudinaryService.deleteJournalImages(uploadedImages);

            throw ex;
        }
    }

    @Transactional
    public JournalResponseDto deleteJournalImage(
            ObjectId journalId,
            String publicId
    ) {
        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        // Find the image within the journal
        JournalImage image = findJournalImage(journal, publicId);

        // Is the image deleted is coverImage?
        boolean isCoverImage = Objects.equals(image.getImageUrl(), journal.getCoverImageUrl());

        // Delete the image from Cloudinary
        cloudinaryService.deleteImage(publicId);

        // Remove the image from the journal
        journal.getImages().remove(image);

        // determine the coverImageUrl...
        if (isCoverImage) {
            journal.setCoverImageUrl(determineCoverImageUrl(journal.getImages()));
        }

        // Persist the updated journal
        Journal updatedJournal = journalRepository.save(journal);

        return journalMapper.toResponseDto(updatedJournal);
    }

    @Transactional
    public JournalResponseDto replaceJournalImage(
            ObjectId journalId,
            String publicId,
            MultipartFile image
    ) {

        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        JournalImage existingImage = findJournalImage(journal, publicId);

        // Check whether the image being replaced is the current cover image
        boolean isCoverImage = Objects.equals(existingImage.getImageUrl(), journal.getCoverImageUrl());

        // Upload the replacement image
        JournalImage uploadedImage = cloudinaryService.uploadJournalImage(image);

        String oldPublicId = existingImage.getPublicId();

        try {

            // Replace image metadata
            existingImage.setImageUrl(uploadedImage.getImageUrl());

            existingImage.setPublicId(uploadedImage.getPublicId());

            // Update the cover image if the replaced image was the cover
            if (isCoverImage) {
                journal.setCoverImageUrl(uploadedImage.getImageUrl());
            }

            Journal updatedJournal = journalRepository.save(journal);

            // Best-effort cleanup of the old image
            try {
                cloudinaryService.deleteImage(oldPublicId);

            } catch (Exception ex) {

                log.warn(
                        "Failed to delete old journal image: {}",
                        oldPublicId,
                        ex
                );
            }

            return journalMapper.toResponseDto(updatedJournal);

        } catch (Exception ex) {

            try {

                // Database failed.
                // Remove the newly uploaded image.
                cloudinaryService.deleteImage(
                        uploadedImage.getPublicId()
                );
            } catch (Exception cleanupEx) {
                log.warn(
                        "Failed to cleanup uploaded replacement image: {}",
                        uploadedImage.getPublicId(),
                        cleanupEx
                );
            }

            throw ex;
        }
    }

    public JournalPageResponseDto getJournals(
            JournalSearchCriteria criteria,
            Pageable pageable) {

        String username = getLoggedInUser();

        User user = userService.findUserByUserName(username);

        List<Journal> filteredJournals = getJournals(criteria, user);

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        int start = (int) pageable.getOffset();

        if (start >= filteredJournals.size()) {

            return JournalPageResponseDto.builder()
                    .journals(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(filteredJournals.size())
                    .totalPages((int) Math.ceil((double) filteredJournals.size() / size))
                    .first(page == 0)
                    .last(true)
                    .build();
        }

        int end = Math.min(start + size, filteredJournals.size());

        List<JournalSummaryDto> summaries = filteredJournals
                .subList(start, end)
                .stream()
                .map(journalMapper::toSummaryDto)
                .toList();

        return JournalPageResponseDto.builder()
                .journals(summaries)
                .page(page)
                .size(size)
                .totalElements(filteredJournals.size())
                .totalPages((int) Math.ceil((double) filteredJournals.size() / size))
                .first(page == 0)
                .last(end >= filteredJournals.size())
                .build();
    }

    @Transactional
    public JournalResponseDto setCoverImage(
            ObjectId journalId,
            String publicId
    ) {

        User user = getAuthenticatedUser();

        Journal journal = findUserJournal(user, journalId);

        JournalImage image = findJournalImage(journal, publicId);

        journal.setCoverImageUrl(
                image.getImageUrl()
        );

        Journal updatedJournal = journalRepository.save(journal);

        return journalMapper.toResponseDto(updatedJournal);
    }

    @Transactional
    public JournalResponseDto updateFavorite(
            ObjectId journalId,
            UpdateFavoriteRequestDto request
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserJournal(user, journalId);

        journal.setFavorite(request.getFavorite());

        Journal updatedJournal =
                journalRepository.save(journal);

        return journalMapper.toResponseDto(updatedJournal);
    }

    public List<JournalResponseDto> getAllJournals() {

        User user = getAuthenticatedUser();

        return user.getJournals()
                .stream()
                .sorted(
                        Comparator.comparing(Journal::getCreatedAt).reversed()
                )
                .map(journalMapper::toResponseDto)
                .toList();
    }
}