package com.job.management.service.quartz.controller;

import com.job.management.service.quartz.dto.ServerResponse;
import com.job.management.service.quartz.job.CronJob;
import com.job.management.service.quartz.job.SimpleJob;
import com.job.management.service.quartz.service.JobService;
import com.job.management.service.quartz.util.ServerResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/scheduler/")
public class JobController {

    @Autowired
    @Lazy
    JobService jobService;

    @GetMapping("schedule")
    public ServerResponse schedule(@RequestParam("jobName") String jobName,
                                   @RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
                                   @RequestParam("cronExpression") String cronExpression) {
        log.info("JobController.schedule()");

        //Job Name is mandatory
        if (jobName == null || jobName.trim().equals("")) {
            return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
        }

        //Check if job Name is unique;
        if (!jobService.isJobWithNamePresent(jobName)) {

            if (cronExpression == null || cronExpression.trim().equals("")) {
                //Single Trigger
                boolean status = jobService.scheduleOneTimeJob(jobName, SimpleJob.class, jobScheduleTime);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }

            } else {
                //Cron Trigger
                boolean status = jobService.scheduleCronJob(jobName, CronJob.class, jobScheduleTime, cronExpression);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }
            }
        } else {
            return getServerResponse(ServerResponseCode.JOB_WITH_SAME_NAME_EXIST, false);
        }
    }

    @GetMapping("unschedule")
    public void unschedule(@RequestParam("jobName") String jobName) {
        log.info("JobController.unschedule()");
        jobService.unScheduleJob(jobName);
    }

    @GetMapping("delete")
    public ServerResponse delete(@RequestParam("jobName") String jobName) {
        log.info("JobController.delete()");

        if (jobService.isJobWithNamePresent(jobName)) {
            boolean isJobRunning = jobService.isJobRunning(jobName);

            if (!isJobRunning) {
                boolean status = jobService.deleteJob(jobName);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, true);
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }
            } else {
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
            }
        } else {
            //Job doesn't exist
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    @GetMapping("pause")
    public ServerResponse pause(@RequestParam("jobName") String jobName) {
        log.info("JobController.pause()");

        if (jobService.isJobWithNamePresent(jobName)) {

            boolean isJobRunning = jobService.isJobRunning(jobName);

            if (!isJobRunning) {
                boolean status = jobService.pauseJob(jobName);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, true);
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }
            } else {
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
            }

        } else {
            //Job doesn't exist
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    @GetMapping("resume")
    public ServerResponse resume(@RequestParam("jobName") String jobName) {
        log.info("JobController.resume()");

        if (jobService.isJobWithNamePresent(jobName)) {
            String jobState = jobService.getJobState(jobName);

            if (jobState.equals("PAUSED")) {
                log.info("Job current state is PAUSED, Resuming job...");
                boolean status = jobService.resumeJob(jobName);

                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, true);
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }
            } else {
                return getServerResponse(ServerResponseCode.JOB_NOT_IN_PAUSED_STATE, false);
            }

        } else {
            //Job doesn't exist
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    @GetMapping("update")
    public ServerResponse updateJob(@RequestParam("jobName") String jobName,
                                    @RequestParam("jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
                                    @RequestParam("cronExpression") String cronExpression) {
        log.info("JobController.updateJob()");

        //Job Name is mandatory
        if (jobName == null || jobName.trim().equals("")) {
            return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
        }

        //Edit Job
        if (jobService.isJobWithNamePresent(jobName)) {

            if (cronExpression == null || cronExpression.trim().equals("")) {
                //Single Trigger
                boolean status = jobService.updateOneTimeJob(jobName, jobScheduleTime);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }

            } else {
                //Cron Trigger
                boolean status = jobService.updateCronJob(jobName, jobScheduleTime, cronExpression);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, jobService.getAllJobs());
                } else {
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }
            }


        } else {
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    @GetMapping("jobs")
    public ServerResponse getAllJobs() {
        log.info("JobController.getAllJobs()");

        List<Map<String, Object>> list = jobService.getAllJobs();
        return getServerResponse(ServerResponseCode.SUCCESS, list);
    }

    @GetMapping("checkJobName")
    public ServerResponse checkJobName(@RequestParam("jobName") String jobName) {
        log.info("JobController.checkJobName()");

        //Job Name is mandatory
        if (jobName == null || jobName.trim().equals("")) {
            return getServerResponse(ServerResponseCode.JOB_NAME_NOT_PRESENT, false);
        }

        boolean status = jobService.isJobWithNamePresent(jobName);
        return getServerResponse(ServerResponseCode.SUCCESS, status);
    }

    @GetMapping("isJobRunning")
    public ServerResponse isJobRunning(@RequestParam("jobName") String jobName) {
        log.info("JobController.isJobRunning()");

        boolean status = jobService.isJobRunning(jobName);
        return getServerResponse(ServerResponseCode.SUCCESS, status);
    }

    @GetMapping("jobState")
    public ServerResponse getJobState(@RequestParam("jobName") String jobName) {
        log.info("JobController.getJobState()");

        String jobState = jobService.getJobState(jobName);
        return getServerResponse(ServerResponseCode.SUCCESS, jobState);
    }

    @GetMapping("stop")
    public ServerResponse stopJob(@RequestParam("jobName") String jobName) {
        log.info("JobController.stopJob()");

        if (jobService.isJobWithNamePresent(jobName)) {

            if (jobService.isJobRunning(jobName)) {
                boolean status = jobService.stopJob(jobName);
                if (status) {
                    return getServerResponse(ServerResponseCode.SUCCESS, true);
                } else {
                    //Server error
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }

            } else {
                //Job not in running state
                return getServerResponse(ServerResponseCode.JOB_NOT_IN_RUNNING_STATE, false);
            }

        } else {
            //Job doesn't exist
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    @GetMapping("start")
    public ServerResponse startJobNow(@RequestParam("jobName") String jobName) {
        log.info("JobController.startJobNow()");

        if (jobService.isJobWithNamePresent(jobName)) {

            if (!jobService.isJobRunning(jobName)) {
                boolean status = jobService.startJobNow(jobName);

                if (status) {
                    //Success
                    return getServerResponse(ServerResponseCode.SUCCESS, true);

                } else {
                    //Server error
                    return getServerResponse(ServerResponseCode.ERROR, false);
                }

            } else {
                //Job already running
                return getServerResponse(ServerResponseCode.JOB_ALREADY_IN_RUNNING_STATE, false);
            }

        } else {
            //Job doesn't exist
            return getServerResponse(ServerResponseCode.JOB_DOESNT_EXIST, false);
        }
    }

    private ServerResponse getServerResponse(int responseCode, Object data) {
        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatusCode(responseCode);
        serverResponse.setData(data);
        return serverResponse;
    }
}
