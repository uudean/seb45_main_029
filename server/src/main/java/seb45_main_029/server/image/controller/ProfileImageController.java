package seb45_main_029.server.image.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import seb45_main_029.server.image.entity.Image;
import seb45_main_029.server.image.mapper.ImageMapper;
import seb45_main_029.server.image.repository.ImageRepository;
import seb45_main_029.server.image.service.ProfileImageService;
import seb45_main_029.server.response.SingleResponseDto;
import seb45_main_029.server.user.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/profile")
@RestController
public class ProfileImageController {

    private final ProfileImageService profileImageService;
    private final ImageMapper imageMapper;
    private final ImageRepository imageRepository;
    private final UserService userService;

    //    프로필 이미지 업로드
    @PostMapping("/upload")
    public ResponseEntity<List<String>> profileImageUpload(@RequestPart List<MultipartFile> multipartFile) throws Exception {
        return new ResponseEntity<>(profileImageService.profileUpload(multipartFile), HttpStatus.OK);
    }

    //    프로필 이미지 수정
    @PatchMapping("/update")
    public ResponseEntity<List<String>> profileImageUpdate(@RequestPart List<MultipartFile> multipartFile) throws Exception {
        return new ResponseEntity<>(profileImageService.profileUpdate(multipartFile), HttpStatus.OK);
    }

    //    프로필 이미지 조회
    @GetMapping("/{user-id}")
    public ResponseEntity findUserProfileImage(@PathVariable("user-id") long userId) {
        Image response = profileImageService.getUserProfileImage(userId);
        return new ResponseEntity<>(new SingleResponseDto<>(imageMapper.imageToImageResponseDto(response)), HttpStatus.OK);
    }

    //    프로필 이미지 삭제
    @DeleteMapping("/delete/{user-id}")
    public ResponseEntity deleteImage(@PathVariable("user-id") long userId) {
        profileImageService.deleteDbImg(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
