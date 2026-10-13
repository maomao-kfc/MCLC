package com.youlai.boot;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动类
 *
 * @author Ray.Hao
 * @since 0.0.1
   */
@Slf4j
@SpringBootApplication
@MapperScan("com.youlai.boot.**.mapper")
@EnableScheduling
public class YouLaiBootApplication {

    /** JODConverter LibreOffice 进程池端口（与 application-*.yml 中 jodconverter.local.port-numbers 一致） */
    private static final int[] JODCONVERTER_PORTS = {2002, 2003, 2004};

    public static void main(String[] args) {
        killResidualSofficeProcesses();
        SpringApplication.run(YouLaiBootApplication.class, args);
    }

    /**
     * 清理残留的 LibreOffice（soffice）进程
     *
     * 问题场景：上次应用异常退出（如 IDEA 强制 Stop、taskkill /F）时，
     * JODConverter 启动的 soffice 子进程未被正确终止，成为孤儿进程。
     * 残留进程锁住 profile 目录中的 extensions.pmap 文件，导致下次启动时
     * JODConverter 清理旧 profile 目录失败（FileSystemException: 另一个程序正在使用此文件）。
     *
     * 处理策略：仅在 Windows 环境下，通过端口精确定位 JODConverter 管理的 soffice 进程并终止，
     * 避免误杀用户正在使用的 LibreOffice 桌面实例。
     * 清理失败不影响启动，JODConverter 自身有 connect_or_kill 回退策略。
     */
    private static void killResidualSofficeProcesses() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }
        for (int port : JODCONVERTER_PORTS) {
            try {
                Process netstat = Runtime.getRuntime().exec(
                        new String[]{"cmd", "/c", "netstat -ano | findstr :" + port + " | findstr LISTENING"});
                String output = new String(netstat.getInputStream().readAllBytes());
                netstat.waitFor();
                for (String line : output.split("\r?\n")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        String pid = parts[parts.length - 1];
                        Runtime.getRuntime().exec(new String[]{"taskkill", "/PID", pid, "/F"});
                        log.info("已终止占用端口 {} 的残留 LibreOffice 进程 PID={}", port, pid);
                    }
                }
            } catch (Exception e) {
                log.warn("清理端口 {} 残留进程失败（不影响启动）: {}", port, e.getMessage());
            }
        }
    }

}
