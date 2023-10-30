package seb45_main_029.server.batch.youtubeApi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class Scheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job youtubeJob;

    @Scheduled(cron = "0 */1 * * * *")
    public void youtubeJobRun() throws JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        JobParameters jobParameters = new JobParameters(Collections.singletonMap("requestTime",new JobParameter(System.currentTimeMillis())));
        jobLauncher.run(youtubeJob,jobParameters);
    }
}
