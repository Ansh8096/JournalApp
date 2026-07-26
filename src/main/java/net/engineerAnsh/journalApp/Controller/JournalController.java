package net.engineerAnsh.journalApp.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Dto.common.MessageResponseDto;
import net.engineerAnsh.journalApp.Dto.journals.*;
import net.engineerAnsh.journalApp.Service.JournalService;
import net.engineerAnsh.journalApp.criteria.JournalSearchCriteria;
import net.engineerAnsh.journalApp.enums.Mood;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import org.bson.types.ObjectId;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Create a new journal entry for the authenticated user")
    @RequestBody( // explicitly tells the OpenAPI generator: "The journal part inside this multipart request is JSON."
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "journal", contentType = MediaType.APPLICATION_JSON_VALUE)
                    }
            )
    )    @PostMapping(
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
    public ResponseEntity<JournalResponseDto> getJournalById(@PathVariable String journalId)
    {
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
    @PatchMapping("/{journalId}")
    public ResponseEntity<JournalResponseDto> updateJournalById(
            @PathVariable String journalId,
            @org.springframework.web.bind.annotation.RequestBody
            UpdateJournalRequestDto request
    ) {
        // validate ObjectId first
        if (!ObjectId.isValid(journalId)) {
            throw new BadRequestException("Invalid journal id");
        }

        JournalResponseDto journalResponse = journalService.updateJournalById(new ObjectId(journalId), request);
        return ResponseEntity.ok(journalResponse);
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

    @Operation(summary = "Get all journals of a user")
    @GetMapping("/all")
    public ResponseEntity<List<JournalResponseDto>> getAllJournals() {

        return ResponseEntity.ok(
                journalService.getAllJournals()
        );
    }
}