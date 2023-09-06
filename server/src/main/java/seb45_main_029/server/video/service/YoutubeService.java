package seb45_main_029.server.video.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import seb45_main_029.server.video.entity.Video;
import seb45_main_029.server.video.entity.YoutubeVideoInfo;
import seb45_main_029.server.video.repository.VideoRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class YoutubeService {
    private final VideoRepository videoRepository;

    private Logger log = LoggerFactory.getLogger(YoutubeService.class);

    public final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public final JsonFactory JSON_FACTORY = new JacksonFactory();

    private final String GOOGLE_YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private final String YOUTUBE_SEARCH_TYPE = "video";
    private final String YOUTUBE_API_APPLICATION = "test";

    private YouTube youTube;

    // API 서비스 초기화 메서드
    public YouTube getService() {
        return youTube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {

            }
        }).setApplicationName(YOUTUBE_API_APPLICATION).build();
    }

    public List<Video> youtubeSearch(String query, String category, long maxResult) {

        log.info("===== 유튜브 API 를 통해 관련 동영상 검색중... =====");
//        초기화
        YouTube youtubeService = getService();
//        리스트에 유튜브 비디오 정보 저장
        List<YoutubeVideoInfo> videoInfo = new ArrayList<>();
        List<Video> savedVideoList = new ArrayList<>();

        try {
            if (youTube != null) {
//                쿼리 파라미터 설정
                YouTube.Search.List search = youTube.search().list(("snippet,id"));
                search.setKey("AIzaSyCde6Qb4XhLvkXJ6tWhcPWj_css263v9yo");
                search.setQ(query);
                search.setMaxResults(maxResult);
                search.setTopicId("/m/0kt51");
                search.setType(YOUTUBE_SEARCH_TYPE);

                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();
                if (searchResultList != null && !searchResultList.isEmpty()) {

                    for (SearchResult result : searchResultList) {

                        YoutubeVideoInfo info = new YoutubeVideoInfo(
                                GOOGLE_YOUTUBE_URL + result.getId().getVideoId(),
                                result.getSnippet().getTitle(),
                                result.getSnippet().getThumbnails().getDefault().getUrl(),
                                result.getSnippet().getDescription());
                        videoInfo.add(info);
                        if ((info.getTitle().contains("재활") || info.getTitle().contains("스트레칭"))) {

//                        조회한 동영상 정보 데이터베이스 저장

                            Video video = new Video(info.getUrl(), info.getTitle(), info.getThumbnailUrl(), info.getDescription(), category);
                            List<String> tag = createTag(query, category);
                            video.setTag(tag);
                            videoRepository.save(video);
                            savedVideoList.add(video);
                        }
                    }

                } else {
                    log.info("===== 검색 결과가 없습니다. =====");
                }

            } else {
                log.warn("YouTube API not initialized correctly!");
            }

        } catch (GoogleJsonResponseException e) {
            log.warn(e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } catch (IOException e) {
            log.warn(e.getCause() + " : " + e.getMessage());
        }

        log.info("===== 검색 완료 =====");

//        저장된 동영상 만 response 로 리턴
        return savedVideoList;
    }

    public List<String> createTag(String description, String query) {
//      좀 더 나은 방법을 생각 해보자
        List<String> tag = new ArrayList<>();
        tag.add("#" + query);
        tag.add("#" + description);

        return tag;
    }
}
