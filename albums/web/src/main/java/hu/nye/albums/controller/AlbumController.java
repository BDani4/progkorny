package hu.nye.albums.controller;

import java.util.List;
import javax.validation.ConstraintViolationException;

import hu.nye.albums.model.dto.AlbumDTO;
import hu.nye.albums.model.exception.NotFoundException;
import hu.nye.albums.model.request.AlbumRequest;
import hu.nye.albums.model.response.AlbumResponse;
import hu.nye.albums.service.AlbumService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller file for Albums.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/albums")
@Slf4j
public class AlbumController {

    private static final String SUCCESS_ATTRIBUTE = "success";
    private static final String ALBUM_ATTRIBUTE = "album";
    private static final String MESSAGE_ATTRIBUTE = "message";
    public static final String REDIRECT_ALBUMS_LIST_HTML_ENDPOINT = "redirect:/albums/list.html";

    private final AlbumService albumService;
    private final Converter<AlbumDTO, AlbumResponse> albumDtoToResponseConverter;
    private final Converter<AlbumRequest, AlbumDTO> albumRequestAlbumDTOConverter;

    @GetMapping(path = "/add.html")
    public String albumAddForm(final Model model) {
        log.info("Visit Album add form page.");
        return "albums/add";
    }

    /**
     * addAlbum function.
     *
     * @param model represents org.springframework.ui Model.
     * @param albumRequest represents album request.
     * @return albums/add form
     */
    @PostMapping(path = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String addAlbum(final Model model,
                                final AlbumRequest albumRequest) {
        log.info("Add Album with name:{}.", albumRequest.getName());
        final AlbumResponse album = albumDtoToResponseConverter.convert(
                albumService.addAlbum(
                        albumRequestAlbumDTOConverter.convert(albumRequest)
                )
        );
        model.addAttribute(SUCCESS_ATTRIBUTE, true);
        model.addAttribute(ALBUM_ATTRIBUTE, album);
        return "albums/add.html";
    }

    /**
     * getAlbums List function.
     *
     * @param model represents org.springframework.ui Model.
     * @return albums/list.
     */
    @GetMapping(path = "/list.html")
    public String getAlbums(final Model model) {
        log.info("Retrieve all Albums.");
        final List<AlbumResponse> albums = albumService.getAllAlbums().stream()
                .map(albumDtoToResponseConverter::convert)
                .toList();
        model.addAttribute("albums", albums);
        return "albums/list";
    }

    /**
     * albumUpdate function.
     *
     * @param redirectAttributes org.springframework.web.servlet.mvc.support Redirect Attributes.
     * @param model represents org.springframework.ui Model.
     * @param id represents album id.
     * @return loads update form..
     */
    @GetMapping(path = "/{id}/edit.html")
    public String albumEditForm(final RedirectAttributes redirectAttributes, final Model model, final @PathVariable("id") Long id) {
        log.info("Load Update form for Album with ID:{}.", id);
        try {
            final AlbumDTO album = albumService.getAlbum(id);
            model.addAttribute(ALBUM_ATTRIBUTE, album);
            return "albums/edit";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, false);
            redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "There is no Album with ID:" + id);
            return REDIRECT_ALBUMS_LIST_HTML_ENDPOINT;
        }
    }

    /**
     * Updates album.
     *
     * @param redirectAttributes org.springframework.web.servlet.mvc.support Redirect Attributes.
     * @param model represents org.springframework.ui Model.
     * @param id represents album id.
     * @param albumRequest represents album request.
     * @return updated album.
     */

    @PostMapping(value = "/edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String editAlbum(final RedirectAttributes redirectAttributes,
                              final Model model,
                              final @RequestParam(value = "id", required = false) Long id,
                              final AlbumRequest albumRequest
    ) {
        log.info("Update Album with ID:{}. {}", id, albumRequest);
        try {
            final AlbumResponse album = albumDtoToResponseConverter.convert(
                    albumService.updateAlbum(id,
                            albumRequestAlbumDTOConverter.convert(albumRequest))
            );
            model.addAttribute(ALBUM_ATTRIBUTE, album);
            return "albums/edit";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute(MESSAGE_ATTRIBUTE, "Nem létezik az album, ID:" + id);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, false);
            return REDIRECT_ALBUMS_LIST_HTML_ENDPOINT;
        }
    }

    /**
     * Removes album.
     *
     * @param redirectAttributes org.springframework.web.servlet.mvc.support Redirect Attributes.
     * @param id represents album id.
     * @return removes album{id}.
     */
    @GetMapping(path = "/remove/{id}")
    public String removeAlbum(final RedirectAttributes redirectAttributes, final @PathVariable("id") Long id) {
        log.info("Remove an Album with ID: {}.", id);
        try {
            albumService.deleteAlbum(id);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, true);
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, false);
        }
        return REDIRECT_ALBUMS_LIST_HTML_ENDPOINT;
    }

}
