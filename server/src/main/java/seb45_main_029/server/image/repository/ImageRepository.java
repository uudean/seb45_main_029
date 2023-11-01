package seb45_main_029.server.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import seb45_main_029.server.image.entity.Image;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Image findImageByOriginalName(String originalName);

    Image findImageByUserUserId(long userId);

    Image findImageByProductProductId(long productId);

    List<Image> findAllByProductProductId(long productId);
}
