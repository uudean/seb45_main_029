package seb45_main_029.server.video.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import seb45_main_029.server.video.entity.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Page<Video> findByBodyPart(PageRequest pageRequest, Video.BodyPart bodyPart);

    Page<Video> findByTitleContaining(PageRequest pageRequest, String keyword);

}
