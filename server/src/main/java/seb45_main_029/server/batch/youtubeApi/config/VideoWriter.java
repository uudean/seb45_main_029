package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Configuration;
import seb45_main_029.server.video.entity.Video;
import seb45_main_029.server.video.repository.VideoRepository;

import java.util.List;

// 데이터를 저장하는 곳
@Slf4j
@RequiredArgsConstructor
@Configuration
public class VideoWriter implements ItemWriter<List<Video>> {

    private final VideoRepository videoRepository;

    @Override
    @StepScope
    public void write(List<? extends List<Video>> videos) {

        log.info("---------------- Writer 실행 ----------------");
//
        for (List<Video> video : videos) {
            videoRepository.saveAll(video);
        }
    }
}
