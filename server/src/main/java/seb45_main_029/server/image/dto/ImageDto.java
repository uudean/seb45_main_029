package seb45_main_029.server.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ImageDto {

    private long imageId;
    private String imageName;
    private String originalName;
    private String imageUrl;
    private Long productId;
    private Long userId;

}
