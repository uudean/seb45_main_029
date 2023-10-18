package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import seb45_main_029.server.video.entity.YoutubeVideoInfo;
import seb45_main_029.server.video.service.YoutubeService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class VideoReader implements ItemReader<YoutubeVideoInfo> {

    private final YoutubeService youtubeService;

    //    job 실행시 파라미터를 입력해 주어야함
//    @Value("#{jobParameters['query']}")
    private String query = "허리";

    //    @Value("#{jobParameters['maxResult']}")
    private long maxResult = 10;
    private List<YoutubeVideoInfo> youtubeVideoInfoList;
    private int currentIndex = 0;

    // api 호출을 통해 동영상 정보 가져옴
    @Override
    @StepScope
    public YoutubeVideoInfo read() {

        log.info("---------------- Reader 시작 ----------------");

        if (youtubeVideoInfoList == null) {
            youtubeVideoInfoList = youtubeService.youtubeSearchBatch(query, maxResult);
        }
        if (currentIndex < youtubeVideoInfoList.size()) {
            YoutubeVideoInfo videoInfo = youtubeVideoInfoList.get(currentIndex);
            currentIndex++;
            return videoInfo;
        }
        // 읽어올 데이터 없으면 read 종료
        return null;
    }
}