package ru.ekbtreeshelp.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.ekbtreeshelp.api.converter.TreeConverter;
import ru.ekbtreeshelp.api.dto.CreateTreeDto;
import ru.ekbtreeshelp.api.dto.UpdateTreeDto;
import ru.ekbtreeshelp.api.dto.TreeDto;
import ru.ekbtreeshelp.api.service.SecurityService;
import ru.ekbtreeshelp.api.service.TreeService;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/tree", produces = MediaType.APPLICATION_JSON_VALUE)
public class TreeController {

    private final TreeService treeService;
    private final TreeConverter treeConverter;
    private final SecurityService securityService;

    @Operation(summary = "Сохраняет новое дерево")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Long create(@RequestBody @Valid CreateTreeDto createTreeDto) {
        return treeService.create(createTreeDto);
    }

    @Operation(summary = "Предоставляет дерево по id")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/get/{id}")
    @PreAuthorize("permitAll()")
    public TreeDto get(@PathVariable Long id) {
        try {
            return treeConverter.toDto(treeService.get(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @Operation(summary = "Редактирует существующее дерево")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority(@Roles.SUPERUSER, @Roles.MODERATOR) " +
            "or hasPermission(#id, @Domains.TREE, @Permissions.EDIT)")
    public void update(@PathVariable Long id, @Valid @RequestBody UpdateTreeDto updateTreeDto) {
        treeService.update(id, updateTreeDto);
    }

    @Operation(summary = "Предоставляет деревья текущего пользователя")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    public List<TreeDto> get() {
        Long authorId = securityService.getCurrentUserId();
        return treeService.getAllByAuthorId(authorId, 1, 20).stream().map(treeConverter::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "Удаляет дерево по id")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority(@Roles.SUPERUSER, @Roles.MODERATOR) " +
            "or hasPermission(#id, @Domains.TREE, @Permissions.DELETE)")
    public void delete(@PathVariable Long id) {
        treeService.delete(id);
    }

    @Operation(
            summary = "Загружает файл в хранилище и прикрепляет его к дереву",
            responses = {
                    @ApiResponse(
                            description = "id загружаемого файла"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Формат multipart/form-data, один файл с ключом 'file'",
                    required = true,
                    content = {
                            @Content(
                                    mediaType = "multipart/form-data",
                                    schema = @Schema(
                                            type = "object",
                                            requiredProperties = "file"
                                    )
                            )
                    }
            )
    )
    @PostMapping("/attachFile/{treeId}")
    @PreAuthorize("isAuthenticated()")
    public Long attachFile(@PathVariable Long treeId, @RequestParam("file") MultipartFile file) {
        return treeService.attachFile(treeId, file);
    }

    @GetMapping("/getAllByAuthorId/{authorId}/{page}/{size}")
    @PreAuthorize("permitAll()")
    List<TreeDto> getAllByAuthorId(@PathVariable("authorId") Long authorId, @PathVariable("page") Integer page, @PathVariable("size") Integer size)
    {
        return treeService.getAllByAuthorId(authorId, page, size).stream()
                .map(treeConverter::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/getAllByAuthorId/{authorId}")
    @PreAuthorize("permitAll()")
    List<TreeDto> getAllByAuthorId(@PathVariable Long authorId)
    {
        return treeService.getAllByAuthorId(authorId, 1, 20).stream()
                .map(treeConverter::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/getAll/{page}/{page}/{size}")
    @PreAuthorize("permitAll()")
    List<TreeDto> getAll(@PathVariable("page") Integer page, @PathVariable("size") Integer size)
    {
        return treeService.listAll(page, size).stream()
                .map(treeConverter::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/getAll")
    @PreAuthorize("permitAll()")
    List<TreeDto> getAll()
    {
        return treeService.listAll(1, 20).stream()
                .map(treeConverter::toDto)
                .collect(Collectors.toList());
    }

}
