package ru.naumen.ectmapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
import java.awt.Color;

@Schema(description = "Транспортная сущность породы дерева")
@Data
@AllArgsConstructor
public class KindOfTreeDto {

    @Schema(description = "Идентификатор породы")
    private Long id;

    @Schema(description = "Название породы")
    private String kind;

    @Schema(description = "Цвет породы")
    private Color color;

    @Schema(description = "Диаметр кроны породы в метрах")
    private Double diameterOfCrown;
}
