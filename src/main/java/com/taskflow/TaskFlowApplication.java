package com.taskflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TaskFlow 启动类
 *
 * @author TaskFlow Team
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.taskflow.mapper")
public class TaskFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
        System.out.println("""

            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║     ████████╗ █████╗ ███████╗██╗  ██╗███████╗██╗      ██╗║
            ║     ╚══██╔══╝██╔══██╗██╔════╝██║ ██╔╝██╔════╝██║      ██║║
            ║        ██║   ███████║███████╗█████╔╝ █████╗  ██║      ██║║
            ║        ██║   ██╔══██║╚════██║██╔═██╗ ██╔══╝  ██║ ██   ██║║
            ║        ██║   ██║  ██║███████║██║  ██╗██║     ███████╗╚█████║
            ║        ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝     ╚══════╝ ╚════╝║
            ║                                                            ║
            ║           现代化智能 RabbitMQ 任务调度平台                  ║
            ║                    启动成功！                              ║
            ║                                                            ║
            ║   接口文档: http://localhost:8080/api/swagger-ui.html      ║
            ║   监控指标: http://localhost:8080/api/actuator             ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            """);
    }

}
