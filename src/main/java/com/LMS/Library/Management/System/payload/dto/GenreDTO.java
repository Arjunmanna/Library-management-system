package com.LMS.Library.Management.System.payload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDTO {

    private Long id;

    @NotBlank(message = "Genre Code Is Mandatory")
    private String code;

    @NotBlank(message = "Genre Name Is Mandatory")
    private String name;

    @Size(max = 500,message = "Description Must Not Exceed 500 Characters")
    private String description;

    @Min(value=0,message = "Display Order Can Not Be Negative")
    private Integer displayOrder=0;

    private Boolean active;

    private Long parentGenreId;

    private String parentGenreName;

    private List<GenreDTO> subGenre;

    private Long bookCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
