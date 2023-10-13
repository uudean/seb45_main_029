package seb45_main_029.server.image.controller;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seb45_main_029.server.image.entity.Image;
import seb45_main_029.server.image.mapper.ImageMapper;
import seb45_main_029.server.image.service.ProductImageService;
import seb45_main_029.server.response.MultiResponseDto;
import seb45_main_029.server.response.SingleResponseDto;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/productImg")
@RestController
public class ProductImageController {

    private final ProductImageService productImageService;
    private final ImageMapper imageMapper;

    //    상품 이미지 등록
    @PostMapping("/upload")
    public ResponseEntity<List<String>> upload(@RequestPart List<MultipartFile> multipartFiles,
                                               @RequestParam long productId) throws Exception {

        return new ResponseEntity<>(productImageService.productImgUpload(multipartFiles, productId), HttpStatus.OK);
    }

    //    상품 이미지 수정
    @PatchMapping("/update/{product-id}")
    public ResponseEntity<List<String>> update(@RequestPart List<MultipartFile> multipartFiles,
                                               @RequestParam List<Long>imageIds,
                                               @PathVariable("product-id") long productId) throws Exception {

        return new ResponseEntity<>(productImageService.productImgUpdate(multipartFiles,productId,imageIds), HttpStatus.OK);
    }

    @GetMapping("/{product-id}")
    public ResponseEntity getProductImages(@PathVariable("product-id") long productId) {

        return new ResponseEntity<>(imageMapper.imagesToImageResponseDtos(productImageService.getImages(productId)),HttpStatus.OK);
    }



    //  상품 이미지 삭제
    @DeleteMapping("/delete/{product-id}")
    public ResponseEntity delete(@PathVariable("product-id") long productId) {
        productImageService.deleteDbImg(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
