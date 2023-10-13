package seb45_main_029.server.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import seb45_main_029.server.exception.BusinessLogicException;
import seb45_main_029.server.exception.ExceptionCode;
import seb45_main_029.server.image.entity.Image;
import seb45_main_029.server.image.repository.ImageRepository;
import seb45_main_029.server.product.entity.Product;
import seb45_main_029.server.product.repository.ProductRepository;
import seb45_main_029.server.product.service.ProductService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService {

    @Autowired
    private AmazonS3 amazonS3;

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ProductService productService;
    private final ProfileImageService profileImageService;

    public ProductImageService(ProductRepository productRepository, ImageRepository imageRepository, ProductService productService, ProfileImageService profileImageService) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.productService = productService;
        this.profileImageService = profileImageService;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    //    상품 이미지 등록
    public List<String> productImgUpload(List<MultipartFile> multipartFiles, long productId) throws IOException {

        List<String> fileNameList = new ArrayList<>();
        String dirName = "productImg/";

//      상품 찾기
        Product findProduct = productService.findProduct(productId);

//      데이터베이스에 해당 상품의 이미지가 존재하는지 검색
        Image existImage = imageRepository.findImageByProductProductId(productId);

//      이미지가 존재하지 않는다면 로직 진행
        if (existImage == null) {

            for (MultipartFile file : multipartFiles) {

                String fileName = createFileName(file.getOriginalFilename());
                String folder = dirName + fileName;

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());

                String bucketUrl = amazonS3.getUrl(bucket, folder).toString();

                try (InputStream inputStream = file.getInputStream()) {
                    amazonS3.putObject(new PutObjectRequest(bucket, folder, inputStream, metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
                fileNameList.add(fileName);

                //   데이터베이스 저장
                Image image = new Image();
                image.setOriginalName(file.getOriginalFilename());
                image.setImageName(fileName);
                image.setImageUrl(bucketUrl);
                image.setProduct(findProduct);
                imageRepository.save(image);
            }

        } else throw new BusinessLogicException(ExceptionCode.IMAGE_EXISTS);

        return fileNameList;
    }

    //    상품 이미지 수정
    @Transactional
    public List<String> productImgUpdate(List<MultipartFile> multipartFiles, long productId, List<Long> imageIds) throws IOException {

        List<String> fileNameList = new ArrayList<>();
        String dirName = "productImg/";
        for (Long id : imageIds) {

            Image image = imageRepository.findById(id).orElseThrow();
            String fileName = image.getImageName();
            deleteImage(dirName + fileName);
            imageRepository.deleteById(id);
        }

        for (MultipartFile file : multipartFiles) {
//            원본 파일명
//            String originalName = file.getOriginalFilename();
//            Image findImage = imageRepository.findImageByOriginalName(originalName);

            String newFileName = createFileName(file.getOriginalFilename());
            String folder = dirName + newFileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            String bucketUrl = amazonS3.getUrl(bucket, folder).toString();

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, folder, inputStream, metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            fileNameList.add(newFileName);

            Image image = new Image();
            image.setOriginalName(file.getOriginalFilename());
            image.setImageName(newFileName);
            image.setImageUrl(bucketUrl);
            image.setProduct(productService.findProduct(productId));
            imageRepository.save(image);
//          데이터베이스 저장되어있는 정보 수정
//            findImage.setOriginalName(file.getOriginalFilename());
//            findImage.setImageName(newFileName);
//            findImage.setImageUrl(bucketUrl);
//            imageRepository.save(findImage);
        }

        return fileNameList;
    }

    //    상품 이미지 조회
    public List<Image> getImages(long productId) {

        List<Image> images = imageRepository.findAllByProductProductId(productId);

        return images;
    }

    //  S3에서 이미지 삭제 후 데이터베이스 이미지 삭제
    public void deleteDbImg(long productId) {

        String dirName = "productImg/";
        Image image = imageRepository.findImageByProductProductId(productId);
        deleteImage(dirName + image.getImageName());
        imageRepository.delete(image);

    }

    //  S3에서 이미지 삭제
    public void deleteImage(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    //    확장자명을 포함한 랜덤한 파일명 생성 -> 원본 파일명을 그대로 업로드 하게되면 파일명이 중복될 시 기존 파일이 대체되기 때문에 중복되지 않는 새로운 이름을 부여
    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    //    원본파일 확장자명 추출
    public String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
