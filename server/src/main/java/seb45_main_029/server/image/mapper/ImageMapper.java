package seb45_main_029.server.image.mapper;

import org.mapstruct.Mapper;
import seb45_main_029.server.image.dto.ImageDto;
import seb45_main_029.server.image.entity.Image;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    default ImageDto imageToImageResponseDto(Image image) {

        if (image == null) {
            return null;
        }
        ImageDto imageDto = new ImageDto();

        if (image.getUser() == null) {

            imageDto.setImageId(image.getImageId());
            imageDto.setImageName(image.getImageName());
            imageDto.setOriginalName(image.getOriginalName());
            imageDto.setImageUrl(image.getImageUrl());
            imageDto.setProductId(image.getProduct().getProductId());
            imageDto.setUserId(null);

        } else if (image.getProduct() == null) {

            imageDto.setImageId(image.getImageId());
            imageDto.setImageName(image.getImageName());
            imageDto.setOriginalName(image.getOriginalName());
            imageDto.setImageUrl(image.getImageUrl());
            imageDto.setUserId(image.getUser().getUserId());
            imageDto.setProductId(null);

        }
        return imageDto;
    }

    default List<ImageDto> imagesToImageResponseDtos(List<Image> images) {
        if (images == null) {
            return null;
        }

        List<ImageDto> list = new ArrayList<ImageDto>(images.size());
        for (Image image : images) {
            list.add(imageToImageResponseDto(image));
        }

        return list;
    }
}
