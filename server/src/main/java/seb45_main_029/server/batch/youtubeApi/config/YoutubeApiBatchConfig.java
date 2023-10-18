package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import seb45_main_029.server.video.entity.Video;
import seb45_main_029.server.video.entity.YoutubeVideoInfo;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class YoutubeApiBatchConfig {
    @Autowired
    private final JobBuilderFactory jobBuilderFactory;
    @Autowired
    private final StepBuilderFactory stepBuilderFactory;

    private final VideoReader videoReader;
    private final VideoProcessor videoProcessor;
    private final VideoWriter videoWriter;

    @Bean
    public Job youtubeJob() throws Exception {

        return jobBuilderFactory.get("youtubeJob")
                .start(step1())
                .build();
    }

    @JobScope
    @Bean
    public Step step1() throws Exception {
        log.info("---------------- Step1 시작 ----------------");
        return stepBuilderFactory.get("step1")
//                정해진 chunk 사이즈에 도달하게 되면 writer에게 전달
//                chunk<I> : ItemReader 로 읽은 하나의 아이템을 chunk 수 만큼 반복해서 저장하는 타입
//                chunk<O> : ItemReader 로 부터 받아온 데이터를 ItemProcessor 로 가공한 후 ,  ItemWriter 에 전달되는 타입
                .<YoutubeVideoInfo, List<Video>>chunk(50)
                .reader(videoReader)
                .processor(videoProcessor)
                .writer(videoWriter)
                .build();
    }


}
