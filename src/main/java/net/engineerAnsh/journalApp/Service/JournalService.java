package net.engineerAnsh.journalApp.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.journals.*;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.Entity.User;
import net.engineerAnsh.journalApp.Repository.JournalRepository;
import net.engineerAnsh.journalApp.criteria.JournalSearchCriteria;
import net.engineerAnsh.journalApp.enums.JournalStatus;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import net.engineerAnsh.journalApp.exception.exceptions.ResourceNotFoundException;
import net.engineerAnsh.journalApp.helper.JournalStatisticsCalculator;
import net.engineerAnsh.journalApp.mapper.JournalMapper;
import net.engineerAnsh.journalApp.model.JournalImage;
import net.engineerAnsh.journalApp.validation.DraftValidator;
import net.engineerAnsh.journalApp.validation.TagValidator;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserService userService;
    private final JournalMapper journalMapper;
    private final CloudinaryService cloudinaryService;
    private final TagValidator tagValidator;
    private final JournalStatisticsCalculator statisticsCalculator;
    private final DraftValidator draftValidator;
    private static final int MAX_IMAGES_PER_JOURNAL = 20;

    private String getLoggedInUser() {
        Authentication userAuthenticated = SecurityContextHolder.getContext().getAuthentication(); // 'SecurityContextHolder.getContext().getAuthentication() '-> gives the current logged-in user...
        return userAuthenticated.getName();
    }

    private boolean isPublished(Journal journal) {

        return journal.getStatus() == null
                || journal.getStatus() == JournalStatus.PUBLISHED;
    }

    private Journal findUserPublishedJournal(
            User user,
            ObjectId journalId
    ) {

        Journal journal = findUserJournal(
                user,
                journalId
        );

        if (!isPublished(journal)) {

            throw new ResourceNotFoundException(
                    "Journal not found."
            );
        }

        return journal;
    }

    private void validateImageCount(
            int imagesCount
    ) {

        if (imagesCount > MAX_IMAGES_PER_JOURNAL) {

            throw new BadRequestException(
                    "A journal can contain a maximum of "
                            + MAX_IMAGES_PER_JOURNAL
                            + " images."
            );
        }
    }

    private void validateUpdatedJournalImageLimit(
            Journal journal,
            List<JournalImage> imagesToRemove,
            List<MultipartFile> newImages
    ) {

        int currentImageCount =
                journal.getImages().size();

        int removedImageCount =
                imagesToRemove.size();

        int newImageCount =
                newImages == null
                        ? 0
                        : newImages.size();

        int finalImageCount =
                currentImageCount
                        - removedImageCount
                        + newImageCount;

        validateImageCount(finalImageCount);
    }

    private Journal findUserJournal(
            ObjectId journalId,
            User user
    ) {

        Journal journal =
                journalRepository.findById(journalId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Journal not found."
                                )
                        );

        boolean belongsToUser =
                user.getJournals()
                        .stream()
                        .anyMatch(userJournal ->
                                userJournal.getId()
                                        .equals(journalId)
                        );

        if (!belongsToUser) {

            throw new ResourceNotFoundException(
                    "Journal not found."
            );
        }

        return journal;
    }

    private Comparator<Journal> buildComparator(Pageable pageable) {

        if (pageable.getSort().isUnsorted()) {
            return Comparator.comparing(Journal::getCreatedAt).reversed();
        }

        Comparator<Journal> comparator = null;

        for (Sort.Order order : pageable.getSort()) {

            Comparator<Journal> current = switch (order.getProperty()) {

                case "title" -> Comparator.comparing(
                        Journal::getTitle,
                        String.CASE_INSENSITIVE_ORDER);

                case "createdAt" -> Comparator.comparing(Journal::getCreatedAt);

                case "updatedAt" -> Comparator.comparing(Journal::getUpdatedAt);

                case "mood" -> Comparator.comparing(Journal::getMood);

                case "favorite" -> Comparator.comparing(Journal::isFavorite);

                default -> Comparator.comparing(Journal::getCreatedAt);
            };

            if (order.isDescending()) {
                current = current.reversed();
            }

            comparator = comparator == null
                    ? current
                    : comparator.thenComparing(current);
        }

        return comparator;
    }

    private List<Journal> getJournals(
            JournalSearchCriteria criteria,
            User user,
            Pageable pageable
    ) {

        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        Stream<Journal> stream = user.getJournals().stream().filter(this::isPublished);

        // Search by title, content, or tags
        if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {

            String query = criteria.getQuery().trim().toLowerCase();

            stream = stream.filter(journal ->

                    journal.getTitle().toLowerCase().contains(query)

                            || (journal.getContent() != null
                            && journal.getContent().toLowerCase().contains(query))

                            || journal.getTags().stream()
                            .anyMatch(tag ->
                                    tag.toLowerCase().contains(query))
            );
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

        // Filter by tag
        if (criteria.getTag() != null && !criteria.getTag().isBlank()) {

            String requestedTag = criteria.getTag().trim();

            stream = stream.filter(journal ->
                    journal.getTags().stream()
                            .anyMatch(tag ->
                                    tag.equalsIgnoreCase(requestedTag)));
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

        List<Journal> journals = stream.toList();

        journals = new ArrayList<>(journals);
        journals.sort(buildComparator(pageable));

        return journals;
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
                    "You can upload at most"
                            + MAX_IMAGES_PER_JOURNAL
                            + " images at once."
            );
        }
    }

    private JournalResponseDto removeJournalImage(
            Journal journal,
            String publicId
    ) {

        // Find the image within the journal
        JournalImage image =
                findJournalImage(
                        journal,
                        publicId
                );

        // Check whether the image is the current cover
        boolean isCoverImage =
                Objects.equals(
                        image.getImageUrl(),
                        journal.getCoverImageUrl()
                );

        // Remove the image from the journal
        journal.getImages().remove(
                image
        );

        // Recalculate cover image if necessary
        if (isCoverImage) {

            journal.setCoverImageUrl(
                    determineCoverImageUrl(
                            journal.getImages()
                    )
            );
        }

        // Persist the database change first
        Journal updatedJournal =
                journalRepository.save(
                        journal
                );

        /*
         * Database update succeeded.
         * Now remove the image from Cloudinary.
         */
        try {

            cloudinaryService.deleteImage(
                    publicId
            );

        } catch (Exception ex) {

            /*
             * MongoDB is already updated, so don't fail the
             * request because Cloudinary cleanup failed.
             */
            log.error(
                    "Failed to delete journal image from Cloudinary. " +
                            "publicId={}",
                    publicId,
                    ex
            );
        }

        return journalMapper.toResponseDto(
                updatedJournal
        );
    }

    private JournalResponseDto replaceJournalImageInternal(
            Journal journal,
            String publicId,
            MultipartFile image
    ) {

        JournalImage existingImage =
                findJournalImage(
                        journal,
                        publicId
                );

        // Check whether the image being replaced
        // is the current cover image.
        boolean isCoverImage =
                Objects.equals(
                        existingImage.getImageUrl(),
                        journal.getCoverImageUrl()
                );

        // Upload the replacement image.
        JournalImage uploadedImage =
                cloudinaryService.uploadJournalImage(
                        image
                );

        String oldPublicId =
                existingImage.getPublicId();

        try {

            // Replace image metadata.
            existingImage.setImageUrl(
                    uploadedImage.getImageUrl()
            );

            existingImage.setPublicId(
                    uploadedImage.getPublicId()
            );

            // If the replaced image was the cover,
            // update the cover image URL.
            if (isCoverImage) {

                journal.setCoverImageUrl(
                        uploadedImage.getImageUrl()
                );
            }

            // Persist the journal.
            Journal updatedJournal =
                    journalRepository.save(
                            journal
                    );

            // Database update succeeded.
            // Best-effort cleanup of the old image.
            try {

                cloudinaryService.deleteImage(
                        oldPublicId
                );

            } catch (Exception ex) {

                log.warn(
                        "Failed to delete old journal image: {}",
                        oldPublicId,
                        ex
                );
            }

            return journalMapper.toResponseDto(
                    updatedJournal
            );

        } catch (Exception ex) {

            /*
             * Database update failed.
             *
             * Remove the newly uploaded replacement image
             * so that it does not become an orphaned
             * Cloudinary resource.
             */
            try {

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

    private JournalResponseDto setCoverImageInternal(
            Journal journal,
            String publicId
    ) {

        JournalImage image =
                findJournalImage(
                        journal,
                        publicId
                );

        journal.setCoverImageUrl(
                image.getImageUrl()
        );

        Journal updatedJournal =
                journalRepository.save(
                        journal
                );

        return journalMapper.toResponseDto(
                updatedJournal
        );
    }

    private List<JournalImage> findImagesToRemove(
            Journal journal,
            List<String> publicIds
    ) {

        if (publicIds == null || publicIds.isEmpty()) {
            return List.of();
        }

        return publicIds.stream()
                .map(publicId ->
                        findJournalImage(
                                journal,
                                publicId
                        )
                )
                .distinct()
                .toList();
    }

    private JournalResponseDto updateJournalInternal(
            Journal journal,
            JournalUpdateRequest request,
            List<MultipartFile> images
    ) {

        List<JournalImage> imagesToRemove =
                findImagesToRemove(
                        journal,
                        request.getRemoveImagePublicIds()
                );

        validateUpdatedJournalImageLimit(
                journal,
                imagesToRemove,
                images
        );

        List<JournalImage> newImages =
                cloudinaryService.uploadJournalImages(
                        images
                );

        try {

            // Update title
            if (request.getTitle() != null &&
                    !request.getTitle().isBlank()) {

                journal.setTitle(
                        request.getTitle()
                );
            }

            // Update content
            if (request.getContent() != null &&
                    !request.getContent().isBlank()) {

                journal.setContent(
                        request.getContent()
                );
            }

            // Update mood
            if (request.getMood() != null) {

                journal.setMood(
                        request.getMood()
                );
            }

            // Update tags
            if (request.getTags() != null) {

                List<String> normalizedTags =
                        tagValidator.normalize(
                                request.getTags()
                        );

                journal.setTags(
                        normalizedTags
                );
            }

            // Remove images
            if (!imagesToRemove.isEmpty()) {

                boolean isCoverImageRemoved =
                        imagesToRemove.stream()
                                .anyMatch(image ->
                                        Objects.equals(
                                                image.getImageUrl(),
                                                journal.getCoverImageUrl()
                                        )
                                );

                journal.getImages().removeAll(
                        imagesToRemove
                );

                if (isCoverImageRemoved) {

                    journal.setCoverImageUrl(
                            determineCoverImageUrl(
                                    journal.getImages()
                            )
                    );
                }
            }

            // Add new images
            if (!newImages.isEmpty()) {

                journal.getImages().addAll(
                        newImages
                );
            }

            // Assign default cover if needed
            assignDefaultCoverIfMissing(
                    journal,
                    newImages
            );

            // Save
            Journal updatedJournal =
                    journalRepository.save(
                            journal
                    );

            // Cleanup old images after DB success
            if (!imagesToRemove.isEmpty()) {

                cloudinaryService.deleteJournalImages(
                        imagesToRemove
                );
            }

            return journalMapper.toResponseDto(
                    updatedJournal
            );

        } catch (Exception ex) {

            // Cleanup newly uploaded images
            if (!newImages.isEmpty()) {

                cloudinaryService.deleteJournalImages(
                        newImages
                );
            }

            throw ex;
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

            List<String> normalizedTags = tagValidator.normalize(request.getTags());

            // Build the journal
            Journal journal = Journal.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .mood(request.getMood())
                    .coverImageUrl(coverImageUrl)
                    .tags(normalizedTags)
                    .images(journalImages)
                    .status(JournalStatus.PUBLISHED)
                    .build();

            // Save the journal
            Journal savedJournal = journalRepository.save(journal);

            // Associate the journal with the current user
            user.getJournals().add(savedJournal);

            userService.saveUser(user);

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

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        // Delete associated Cloudinary images (best effort)
        cloudinaryService.deleteJournalImages(journal.getImages());

        // Remove the journal reference from the user
        user.getJournals().remove(journal);

        // Delete the journal document
        journalRepository.delete(journal);

        // Persist the updated user
        userService.saveUser(user);
    }

    public JournalResponseDto getJournalById(
            ObjectId journalId
    ) {
        User user = getAuthenticatedUser();

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        return journalMapper.toResponseDto(
                journal
        );
    }

    @Transactional
    public JournalResponseDto updateJournalById(
            ObjectId journalId,
            UpdateJournalRequestDto request,
            List<MultipartFile> images
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        return updateJournalInternal(
                journal,
                request,
                images
        );
    }

    @Transactional
    public JournalResponseDto uploadJournalImages(
            ObjectId journalId,
            List<MultipartFile> images
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

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

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        return removeJournalImage(
                journal,
                publicId
        );
    }

    @Transactional
    public JournalResponseDto replaceJournalImage(
            ObjectId journalId,
            String publicId,
            MultipartFile image
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        return replaceJournalImageInternal(
                journal,
                publicId,
                image
        );
    }


    public JournalPageResponseDto getJournals(
            JournalSearchCriteria criteria,
            Pageable pageable) {

        String username = getLoggedInUser();

        User user = userService.findUserByUserName(username);

        List<Journal> filteredJournals = getJournals(criteria, user, pageable);

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

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        return setCoverImageInternal(
                journal,
                publicId
        );
    }

    @Transactional
    public JournalResponseDto updateFavorite(
            ObjectId journalId,
            UpdateFavoriteRequestDto request
    ) {

        User user = getAuthenticatedUser();

        Journal journal =
                findUserPublishedJournal(
                        user,
                        journalId
                );

        journal.setFavorite(request.getFavorite());

        Journal updatedJournal =
                journalRepository.save(journal);

        return journalMapper.toResponseDto(updatedJournal);
    }

    @Transactional(readOnly = true)
    public JournalStatisticsResponseDto getJournalStatistics() {

        User user = getAuthenticatedUser();

        return statisticsCalculator.calculate(
                user.getJournals().stream().filter(this::isPublished).toList()
        );
    }

    @Transactional
    public JournalResponseDto createDraft(
            CreateDraftRequestDto request,
            List<MultipartFile> images
    ) {

        // Get the currently authenticated user
        String username = getLoggedInUser();

        User user = userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        // Upload draft images
        List<JournalImage> journalImages =
                cloudinaryService.uploadJournalImages(images);

        String coverImageUrl =
                determineCoverImageUrl(journalImages);

        try {

            // Normalize tags
            List<String> normalizedTags =
                    tagValidator.normalize(request.getTags());

            // Build draft
            Journal journal = Journal.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .mood(request.getMood())
                    .coverImageUrl(coverImageUrl)
                    .tags(normalizedTags)
                    .images(journalImages)
                    .status(JournalStatus.DRAFT)
                    .build();

            // Save journal
            Journal savedDraft =
                    journalRepository.save(journal);

            // Associate draft with current user
            user.getJournals().add(savedDraft);

            userService.saveUser(user);

            // Return draft
            return journalMapper.toResponseDto(savedDraft);

        } catch (Exception ex) {

            // Best-effort cleanup of uploaded Cloudinary images.
            cloudinaryService.deleteJournalImages(
                    journalImages
            );

            throw ex;
        }
    }

    @Transactional
    public JournalResponseDto updateDraft(
            ObjectId draftId,
            UpdateDraftRequestDto request,
            List<MultipartFile> images
    ) {

        User user = getAuthenticatedUser();

        Journal draft =
                findUserJournal(
                        user,
                        draftId
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        return updateJournalInternal(
                draft,
                request,
                images
        );
    }

    @Transactional(readOnly = true)
    public JournalResponseDto getDraftById(
            ObjectId draftId
    ) {

        String username = getLoggedInUser();

        User user =
                userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found."
            );
        }

        Journal draft =
                findUserJournal(
                        draftId,
                        user
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        return journalMapper.toResponseDto(
                draft
        );
    }

    @Transactional(readOnly = true)
    public JournalPageResponseDto getDrafts(
            Pageable pageable
    ) {

        String username = getLoggedInUser();

        User user =
                userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found."
            );
        }

        List<Journal> drafts =
                user.getJournals()
                        .stream()
                        .filter(journal ->
                                journal.getStatus()
                                        == JournalStatus.DRAFT
                        )
                        .sorted(
                                Comparator.comparing(
                                        Journal::getUpdatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .toList();

        int page =
                pageable.getPageNumber();

        int size =
                pageable.getPageSize();

        int totalElements =
                drafts.size();

        int totalPages =
                size == 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalElements / size
                );

        int start =
                (int) pageable.getOffset();

        if (start >= totalElements) {

            return JournalPageResponseDto.builder()
                    .journals(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .first(page == 0)
                    .last(true)
                    .build();
        }

        int end =
                Math.min(
                        start + size,
                        totalElements
                );

        List<JournalSummaryDto> summaries =
                drafts
                        .subList(start, end)
                        .stream()
                        .map(journalMapper::toSummaryDto)
                        .toList();

        return JournalPageResponseDto.builder()
                .journals(summaries)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(end >= totalElements)
                .build();
    }

    @Transactional
    public MessageResponseDto deleteDraft(
            ObjectId draftId
    ) {

        String username = getLoggedInUser();

        User user =
                userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found."
            );
        }

        Journal draft =
                findUserJournal(
                        draftId,
                        user
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        /*
         * Delete images from Cloudinary before
         * removing the journal.
         */
        cloudinaryService.deleteJournalImages(
                draft.getImages()
        );

        /*
         * Remove from user's journals.
         */
        user.getJournals().removeIf(
                journal ->
                        journal.getId()
                                .equals(draftId)
        );

        /*
         * Delete the journal document.
         */
        journalRepository.delete(draft);

        /*
         * Save updated user.
         */
        userService.saveUser(user);

        return MessageResponseDto.builder()
                .message(
                        "Draft deleted successfully."
                )
                .build();
    }

    @Transactional
    public JournalResponseDto deleteDraftImage(
            ObjectId draftId,
            String publicId
    ) {

        User user = getAuthenticatedUser();

        Journal draft =
                findUserJournal(
                        user,
                        draftId
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        return removeJournalImage(
                draft,
                publicId
        );
    }

    @Transactional
    public JournalResponseDto replaceDraftImage(
            ObjectId draftId,
            String publicId,
            MultipartFile image
    ) {

        User user = getAuthenticatedUser();

        Journal draft =
                findUserJournal(
                        user,
                        draftId
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        return replaceJournalImageInternal(
                draft,
                publicId,
                image
        );
    }

    @Transactional
    public JournalResponseDto setDraftCoverImage(
            ObjectId draftId,
            String publicId
    ) {

        User user = getAuthenticatedUser();

        Journal draft =
                findUserJournal(
                        user,
                        draftId
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new ResourceNotFoundException(
                    "Draft not found."
            );
        }

        return setCoverImageInternal(
                draft,
                publicId
        );
    }

    @Transactional
    public JournalResponseDto publishDraft(
            ObjectId draftId
    ) {

        String username = getLoggedInUser();

        User user =
                userService.findUserByUserName(username);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "User not found."
            );
        }

        Journal draft =
                findUserJournal(
                        draftId,
                        user
                );

        if (draft.getStatus() != JournalStatus.DRAFT) {

            throw new BadRequestException(
                    "Journal is already published."
            );
        }

        /*
         * Validate the draft before publishing.
         */
        draftValidator.validateForPublish(
                draft.getTitle(),
                draft.getContent(),
                draft.getMood()
        );

        /*
         * Normalize tags again before publishing.
         *
         * This ensures the journal stored as PUBLISHED
         * always satisfies the tag rules.
         */
        List<String> normalizedTags =
                tagValidator.normalize(
                        draft.getTags()
                );

        draft.setTags(
                normalizedTags
        );

        draft.setStatus(
                JournalStatus.PUBLISHED
        );

        Journal publishedJournal =
                journalRepository.save(draft);

        return journalMapper.toResponseDto(
                publishedJournal
        );
    }
}