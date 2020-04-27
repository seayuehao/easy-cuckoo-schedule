package org.ecs.schedule.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ecs.schedule.bean.JobInfoBean;
import org.ecs.schedule.enums.CuckooMessageType;
import org.ecs.schedule.net.client.NettyClientSide;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
public class ScriptExecutor {


    public static void exec(JobInfoBean jobInfo) {

        if (null == jobInfo || StringUtils.isEmpty(jobInfo.getJobName())) {
            log.warn("#@#@>>> no invalid exec action commands!");
            return;
        }

        StringBuffer cmd = new StringBuffer(jobInfo.getJobName());
//		客户端脚本执行自动追加参数：script 执行参数  配置参数(日切:txDate【yyyyMMdd】 / 非日切:flowLastTime【时间戳Long】 flowCurTime【时间戳Long】)
        if (StringUtils.isNotEmpty(jobInfo.getCuckooParallelJobArgs())) {
            appendCmd(cmd, jobInfo.getCuckooParallelJobArgs());
        }

        if (jobInfo.isTypeDaily()) {
            appendCmd(cmd, String.valueOf(jobInfo.getTxDate()));
        } else {
            appendCmd(cmd, String.valueOf(jobInfo.getFlowLastTime()));
            appendCmd(cmd, String.valueOf(jobInfo.getFlowCurrTime()));
        }

        Process process = null;
        StringBuffer sbResult = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec(cmd.toString());

            // 用缓冲器读行
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            // 直到读完为止
            while ((line = br.readLine()) != null) {
                sbResult.append(line);
                sbResult.append("\n");
            }

            if (0 == process.waitFor()) {
                // 发送服务端，任务执行完成
                log.info("script exec successfully, script:{}, jobInfo: {}", jobInfo.getJobName(), jobInfo);
                jobInfo.setErrMessage(sbResult.toString());
                NettyClientSide.send(CuckooMessageType.JOBSUCCED, jobInfo);

            } else {

                InputStream errFis = process.getErrorStream();
                // 用一个读输出流类去读
                InputStreamReader errIsr = new InputStreamReader(errFis);
                // 用缓冲器读行
                BufferedReader errBr = new BufferedReader(errIsr);
                String errLline = null;
                // 直到读完为止
                while ((errLline = errBr.readLine()) != null) {
                    sbResult.append(errLline);
                    sbResult.append("\n");
                }
                // 发送服务端，任务执行失败
                log.error("script exec error taskName:{},error:{}", jobInfo.getJobName(), sbResult.toString());
                // 发送服务端，任务执行失败
                jobInfo.setErrMessage(sbResult.toString());
                NettyClientSide.send(CuckooMessageType.JOBFAILED, jobInfo);
            }

        } catch (Exception e) {

            log.error("script exec unknown error taskName:{}", jobInfo.getJobName(), e);
            jobInfo.setErrMessage(e.getMessage());
            NettyClientSide.send(CuckooMessageType.JOBFAILED, jobInfo);
        } finally {

            if (null != process) {
                process.destroy();
            }
        }
    }

    private static void appendCmd(StringBuffer cmd, String str) {
        cmd.append(" ").append(str);
    }

}
