package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Configuration;
import seb45_main_029.server.common.Job;
import seb45_main_029.server.common.PainArea;
import seb45_main_029.server.video.entity.Video;
import seb45_main_029.server.video.entity.YoutubeVideoInfo;
import seb45_main_029.server.video.repository.VideoRepository;

import java.util.ArrayList;
import java.util.List;

// 데이터를 가공하는곳
@Slf4j
@RequiredArgsConstructor
@Configuration
public class VideoProcessor implements ItemProcessor<YoutubeVideoInfo, List<Video>> {

    //    job 실행 시 파라미터 입력해야함
//    @Value("#{jobParameters['videoJob']}")
    private Job videoJob;

    //    @Value("#{jobParameters['painArea']}")
    private PainArea painArea;

    private final VideoRepository videoRepository;

    @Override
    @StepScope
    public List<Video> process(YoutubeVideoInfo youtubeVideoInfo) throws Exception {

        log.info("---------------- Processor 실행 ----------------");

        List<Video> videos = new ArrayList<>();
        String query = youtubeVideoInfo.getQuery();
//
        if (query.equals("사무직") || query.equals("현장직")) {
            painArea = PainArea.UNKNOWN;
            videoJob = Job.valueOf(youtubeVideoInfo.getQuery());
        } else {
            painArea = PainArea.valueOf(youtubeVideoInfo.getQuery());
            videoJob = Job.UNKNOWN;
        }

//      api로 호출한 정보들
        String title = youtubeVideoInfo.getTitle();
        String youtubeLink = youtubeVideoInfo.getUrl();
        String thumbnail = youtubeVideoInfo.getThumbnailUrl();
        String description = youtubeVideoInfo.getDescription();

        Video findVideo = videoRepository.findByTitle(title);

//      데이터베이스에 동영상이 없으면 해당 동영상 리턴

//      동영상 제목에 재활 또는 스트레칭이 포함되었다면 List에 추가
        if (title.contains("재활") || title.contains("스트레칭")) {
            if (findVideo == null) {
                Video video = new Video();
                video.setTitle(title);
                video.setYoutubeLink(youtubeLink);
                video.setThumbnail(thumbnail);
                video.setDescription(description);
                video.setJob(videoJob);
                video.setPainArea(painArea);
                videos.add(video);
            }
        }
        return videos;
    }
}

