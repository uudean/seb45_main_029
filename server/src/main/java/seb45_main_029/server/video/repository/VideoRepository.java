package seb45_main_029.server.video.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import seb45_main_029.server.common.Job;
import seb45_main_029.server.common.PainArea;
import seb45_main_029.server.video.entity.Video;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Page<Video> findByPainArea(PageRequest pageRequest, PainArea painArea);

    @Query("SELECT v FROM Video v WHERE v.job = :job")
    Page<Video> findByJob(PageRequest pageRequest, Job job);

    Page<Video> findByTitleContaining(PageRequest pageRequest, String keyword);

    Video findByTitle(String title);


}
