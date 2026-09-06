package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.journals.*;
import net.engineerAnsh.journalApp.Service.JournalExportService;
import net.engineerAnsh.journalApp.Service.JournalService;
import net.engineerAnsh.journalApp.criteria.JournalSearchCriteria;
import net.engineerAnsh.journalApp.enums.Mood;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import net.engineerAnsh.journalApp.model.JournalPdf;
import org.bson.types.ObjectId;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/journals")
@Tag(name = "Journal APIs", description = "Read, Create, Update or Delete Journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;
    private final JournalExportService journalExportService;

    @Operation(summary = "Create a new journal entry for the authenticated user")
    @RequestBody( // explicitly tells the OpenAPI generator: "The journal part inside this multipart request is JSON."
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "journal", contentType = MediaType.APPLICATION_JSON_VALUE)
                    }
            )
    )
    @PostMapping(
            consumes = {
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE
            }
    )
    public ResponseEntity<JournalResponseDto> createJournal(
            @Valid
            @RequestPart("journal")
            CreateJournalRequestDto request,

            @RequestPart(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images
    ) {
        JournalResponseDto response =
                journalService.createJournal(
                        request,
                        images
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<JournalPageResponseDto> getJournals(
            @RequestParam(required = false) String query,

            @RequestParam(required = false) Mood mood,

            @RequestParam(required = false)
            Boolean favorite,

            @RequestParam(required = false)
            String tag,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @ParameterObject Pageable pageable
    ) {

        JournalSearchCriteria criteria = JournalSearchCriteria.builder()
                .query(query)
                .mood(mood)
                .favorite(favorite)
                .tag(tag)
                .from(from)
                .to(to)
                .build();

        JournalPageResponseDto response =
                journalService.getJournals(
                        criteria,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get journal by its journalId")
    @GetMapping("/{journalId}")
    public ResponseEntity<JournalResponseDto> getJournalById(@PathVariable String journalId) {
        // validate ObjectId first
        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id");
        }

        JournalResponseDto journalResponse = journalService.getJournalById(new ObjectId(journalId));
        return ResponseEntity.ok(journalResponse);

    }

    @Operation(summary = "Delete journal by its journalId")
    @DeleteMapping("/{journalId}")
    public ResponseEntity<MessageResponseDto> deleteJournalById(@PathVariable String journalId)  // '@PathVariable long myId' → Binds the '101' from the URL to the Java variable myId.
    {
        // validate ObjectId first
        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal journalId");
        }

        journalService.deleteJournal(new ObjectId(journalId));

        return ResponseEntity.ok(
                MessageResponseDto.builder()
                        .message("Journal deleted successfully.")
                        .build()
        );
    }


    @Operation(summary = "Update the journal entry of a user by journalId")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(
                                    name = "journal",
                                    contentType =
                                            MediaType.APPLICATION_JSON_VALUE
                            )
                    }
            )
    )
    @PatchMapping(
            value = "/{journalId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> updateJournalById(

            @PathVariable String journalId,

            @RequestPart("journal")
            UpdateJournalRequestDto request,

            @RequestPart(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images
    ) {

        if (!ObjectId.isValid(journalId)) {

            throw new BadRequestException(
                    "Invalid journal id"
            );
        }

        JournalResponseDto response =
                journalService.updateJournalById(
                        new ObjectId(journalId),
                        request,
                        images
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Upload images to a journal")
    @PostMapping(
            value = "/{journalId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> uploadJournalImages(
            @PathVariable String journalId,

            @RequestPart("images")
            List<MultipartFile> images
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalResponseDto response =
                journalService.uploadJournalImages(
                        new ObjectId(journalId),
                        images
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an image from a journal")
    @DeleteMapping("/{journalId}/images")
    public ResponseEntity<JournalResponseDto> deleteJournalImage(

            @PathVariable String journalId,

            @RequestParam("publicId")
            String publicId
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalResponseDto response =
                journalService.deleteJournalImage(
                        new ObjectId(journalId),
                        publicId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace an image from a journal")
    @PutMapping(
            value = "/{journalId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> replaceJournalImage(

            @PathVariable String journalId,

            @RequestParam("publicId")
            String publicId,

            @RequestPart("image")
            MultipartFile image
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalResponseDto response =
                journalService.replaceJournalImage(
                        new ObjectId(journalId),
                        publicId,
                        image
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set the cover image of a journal")
    @PatchMapping("/{journalId}/cover")
    public ResponseEntity<JournalResponseDto> setCoverImage(

            @PathVariable String journalId,

            @RequestParam("publicId")
            String publicId
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalResponseDto response =
                journalService.setCoverImage(
                        new ObjectId(journalId),
                        publicId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set journal as favorite/unfavorite")
    @PatchMapping("/{journalId}/favorite")
    public ResponseEntity<JournalResponseDto> updateFavorite(

            @PathVariable String journalId,

            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            UpdateFavoriteRequestDto request
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalResponseDto response =
                journalService.updateFavorite(
                        new ObjectId(journalId),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get journal statistics")
    @GetMapping("/statistics")
    public ResponseEntity<JournalStatisticsResponseDto> getJournalStatistics() {

        return ResponseEntity.ok(
                journalService.getJournalStatistics()
        );
    }

    @Operation(summary = "Download a journal as PDF")
    @GetMapping("/{journalId}/download")
    public ResponseEntity<ByteArrayResource> downloadJournal(
            @PathVariable String journalId
    ) {

        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id.");
        }

        JournalPdf pdf =
                journalExportService.downloadJournal(
                        new ObjectId(journalId)
                );

        ByteArrayResource resource =
                new ByteArrayResource(
                        pdf.content()
                );

        return ResponseEntity.ok()

                .contentType(MediaType.APPLICATION_PDF)

                .contentLength(
                        pdf.content().length
                )

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                pdf.fileName() +
                                "\""
                )

                .cacheControl(
                        CacheControl.noCache()
                )

                .body(resource);
    }


    @Operation(summary = "Create a new journal draft")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(
                                    name = "draft",
                                    contentType = MediaType.APPLICATION_JSON_VALUE
                            )
                    }
            )
    )
    @PostMapping(
            value = "/drafts",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> createDraft(

            @RequestPart("draft")
            CreateDraftRequestDto request,

            @RequestPart(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images
    ) {

        JournalResponseDto response =
                journalService.createDraft(
                        request,
                        images
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Update a journal draft")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(
                                    name = "draft",
                                    contentType =
                                            MediaType.APPLICATION_JSON_VALUE
                            )
                    }
            )
    )
    @PatchMapping(
            value = "/drafts/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> updateDraft(

            @PathVariable String id,

            @RequestPart("draft")
            UpdateDraftRequestDto request,

            @RequestPart(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images
    ) {

        if (!ObjectId.isValid(id)) {

            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.updateDraft(
                        new ObjectId(id),
                        request,
                        images
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all journal drafts")
    @GetMapping("/drafts")
    public ResponseEntity<JournalPageResponseDto> getDrafts(

            @RequestParam(required = false)
            String query,

            @RequestParam(required = false)
            Mood mood,

            @RequestParam(required = false)
            String tag,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate to,

            @ParameterObject
            @PageableDefault(
                    sort = "updatedAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        JournalSearchCriteria criteria =
                JournalSearchCriteria.builder()
                        .query(query)
                        .mood(mood)
                        .tag(tag)
                        .from(from)
                        .to(to)
                        .build();

        JournalPageResponseDto response =
                journalService.getDrafts(
                        criteria,
                        pageable
                );

        return ResponseEntity.ok(
                response
        );
    }

    @Operation(summary = "Get draft overview")
    @GetMapping("/drafts/overview")
    public ResponseEntity<DraftOverviewDto> getDraftOverview() {

        DraftOverviewDto response =
                journalService.getDraftOverview();

        return ResponseEntity.ok(
                response
        );
    }

    @Operation(summary = "Get a journal draft by id")
    @GetMapping("/drafts/{id}")
    public ResponseEntity<JournalResponseDto> getDraftById(

            @PathVariable String id
    ) {

        if (!ObjectId.isValid(id)) {
            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.getDraftById(
                        new ObjectId(id)
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a journal draft")
    @DeleteMapping("/drafts/{id}")
    public ResponseEntity<MessageResponseDto> deleteDraft(

            @PathVariable String id
    ) {

        if (!ObjectId.isValid(id)) {
            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        MessageResponseDto response =
                journalService.deleteDraft(
                        new ObjectId(id)
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an image from a journal draft")
    @DeleteMapping("/drafts/{id}/images")
    public ResponseEntity<JournalResponseDto> deleteDraftImage(

            @PathVariable String id,

            @RequestParam("publicId")
            String publicId
    ) {

        if (!ObjectId.isValid(id)) {

            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.deleteDraftImage(
                        new ObjectId(id),
                        publicId
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Publish a journal draft")
    @PostMapping("/drafts/{id}/publish")
    public ResponseEntity<JournalResponseDto> publishDraft(
            @PathVariable String id
    ) {

        if (!ObjectId.isValid(id)) {
            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.publishDraft(
                        new ObjectId(id)
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Replace an image in a journal draft")
    @PutMapping(
            value = "/drafts/{id}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<JournalResponseDto> replaceDraftImage(

            @PathVariable String id,

            @RequestParam("publicId")
            String publicId,

            @RequestPart("image")
            MultipartFile image
    ) {

        if (!ObjectId.isValid(id)) {

            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.replaceDraftImage(
                        new ObjectId(id),
                        publicId,
                        image
                );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set a cover image for a journal draft")
    @PatchMapping("/drafts/{id}/cover")
    public ResponseEntity<JournalResponseDto> setDraftCoverImage(

            @PathVariable String id,

            @RequestParam("publicId")
            String publicId
    ) {

        if (!ObjectId.isValid(id)) {

            throw new BadRequestException(
                    "Invalid draft id."
            );
        }

        JournalResponseDto response =
                journalService.setDraftCoverImage(
                        new ObjectId(id),
                        publicId
                );

        return ResponseEntity.ok(response);
    }

}