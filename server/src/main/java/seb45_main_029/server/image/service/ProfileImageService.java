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
import seb45_main_029.server.user.entity.User;
import seb45_main_029.server.user.service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class ProfileImageService {

    @Autowired
    private AmazonS3 amazonS3;

    private final ImageRepository imageRepository;
    private final UserService userService;

    public ProfileImageService(ImageRepository imageRepository, UserService userService) {
        this.imageRepository = imageRepository;
        this.userService = userService;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //   유저 프로필 이미지 업로드
    @Transactional
    public List<String> profileUpload(List<MultipartFile> multipartFiles) throws IOException {

        List<String> fileNameList = new ArrayList<>();

//        S3 폴더명
        String dirName = "profileImg/";

//        이미지 업로드
        upload(multipartFiles, dirName, fileNameList);

        return fileNameList;
    }

    //  프로필 이미지 업데이트
    @Transactional
    public List<String> profileUpdate(List<MultipartFile> multipartFiles) throws IOException {

        long loginUserId = userService.getLoginUser().getUserId();

        Image findImage = imageRepository.findImageByUserUserId(loginUserId);
        String dirName = "profileImg/";

//        기존 이미지 삭제
        if (findImage != null) {
            deleteImage(dirName + findImage.getImageName());
        } else throw new BusinessLogicException(ExceptionCode.IMAGE_NOT_FOUND);

        List<String> fileNameList = new ArrayList<>();

//        기존 이미지 삭제 후 재 업로드
        upload(multipartFiles, dirName, fileNameList);
        return fileNameList;
    }

    private void existedProfileImg(MultipartFile file, Image existImage, String fileName, String bucketUrl, User loginUser) {

        if (existImage != null) {

            existImage.setOriginalName(file.getOriginalFilename());
            existImage.setImageName(fileName);
            existImage.setImageUrl(bucketUrl);
            imageRepository.save(existImage);

        } else {

            Image newImage = new Image();
            newImage.setOriginalName(file.getOriginalFilename());
            newImage.setImageName(fileName);
            newImage.setImageUrl(bucketUrl);
            newImage.setUser(loginUser);
            loginUser.setImage(newImage);
            imageRepository.save(newImage);

        }
    }
    private void upload(List<MultipartFile> multipartFiles, String dirName, List<String> fileNameList) {
        for (MultipartFile file : multipartFiles) {

            String fileName = createFileName(file.getOriginalFilename());
            String folder = dirName + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            String bucketUrl = amazonS3.getUrl(bucket, folder).toString();

            User loginUser = userService.getLoginUser();
            long userId = loginUser.getUserId();

            Image existImage = imageRepository.findImageByUserUserId(userId);

            existedProfileImg(file, existImage, fileName, bucketUrl, loginUser);


            try (InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, folder, inputStream, metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            fileNameList.add(fileName);
        }
    }

    //  이미지 조회
    @Transactional(readOnly = true)
    public Image getUserProfileImage(long userId) {
        Image findImage = imageRepository.findImageByUserUserId(userId);
        return findImage;
    }

    //  S3 이미지 삭제
    public void deleteImage(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }
//    S3 및 데이터 베이스 이미지 삭제
    @Transactional
    public void deleteDbImg(long userId) {

        String dirName = "profileImg/";
        Image image = imageRepository.findImageByUserUserId(userId);
        deleteImage(dirName + image.getImageName());
        imageRepository.delete(image);

    }

    //    확장자명을 포함한 랜덤한 파일명 생성 -> 원본 파일명을 그대로 업로드 하게되면 파일명이 중복될 시 기존 파일이 대체되기 때문에 중복되지 않는 새로운 이름을 부여
    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // 원본파일 확장자 추출
    public String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
