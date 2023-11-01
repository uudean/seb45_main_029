package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import seb45_main_029.server.video.entity.YoutubeVideoInfo;
import seb45_main_029.server.video.service.YoutubeService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@StepScope
@Configuration
public class VideoReader implements ItemReader<YoutubeVideoInfo> {

    private final YoutubeService youtubeService;

    //    @Value("#{jobParameters['query']}")
    private String[] query = new String[]{"가슴", "다리", "등", "머리", "무릎", "발", "손", "어깨", "팔", "허리", "사무직", "현장직"};
    //    private String query ;
    //    job 실행시 파라미터를 입력해 주어야함
    //    "가슴", "다리", "등", "머리", "무릎", "발", "손", "어깨", "팔", "허리", "사무직", "현장직"
    private static long maxResult = 100;
    private List<YoutubeVideoInfo> youtubeVideoInfoList;
    private YoutubeVideoInfo videoInfo;
    private int currentIndex = 0;
    private int currentQueryIndex = 0;

    // api 호출을 통해 동영상 정보 가져옴
    @Override
    public YoutubeVideoInfo read() {

        log.info("---------------- Reader 시작 ----------------");
//        현재 쿼리의 인덱스가 쿼리 배열 크기보다 작을때 까지 반복한다
//
        while (currentQueryIndex < query.length) {
            // YoutubeVideoInfoList가 null일 경우 또는 현재 인덱스가 유튜브 비디오 리스트의 사이즈보다 작거나 같다면
            if (youtubeVideoInfoList == null || currentIndex >= youtubeVideoInfoList.size()) {
                youtubeVideoInfoList = youtubeService.youtubeSearchBatch(query[currentQueryIndex], maxResult);
                currentIndex = 0;
                currentQueryIndex++;
            } else {
                // 현재 쿼리의 결과가 더 있을 때까지 반복해서 반환합니다.
                videoInfo = youtubeVideoInfoList.get(currentIndex);
                currentIndex++;
                return videoInfo;
            }
        }
// 읽어올 데이터 없으면 read 종료
        return null;
    }
}



