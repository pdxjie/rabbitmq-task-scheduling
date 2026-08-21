package com.taskflow.controller;

import com.taskflow.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Cron 表达式工具控制器
 */
@Tag(name = "Cron 工具", description = "Cron 表达式验证和说明")
@RestController
@RequestMapping("/cron")
public class CronController {

    @Operation(summary = "验证 Cron 表达式")
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateCron(String expression) {
        try {
            org.springframework.scheduling.support.CronExpression cron =
                    org.springframework.scheduling.support.CronExpression.parse(expression);

            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("expression", expression);
            data.put("nextExecuteTime", cron.next(java.time.LocalDateTime.now()));

            return Result.success("Cron 表达式有效", data);
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<>();
            data.put("valid", false);
            data.put("error", e.getMessage());
            return Result.error("Cron 表达式无效");
        }
    }

    @Operation(summary = "Cron 表达式示例")
    @GetMapping("/examples")
    public Result<Map<String, String>> getCronExamples() {
        Map<String, String> examples = new HashMap<>();
        examples.put("每分钟执行", "0 * * * * ?");
        examples.put("每5分钟执行", "0 */5 * * * ?");
        examples.put("每小时执行", "0 0 * * * ?");
        examples.put("每天0点执行", "0 0 0 * * ?");
        examples.put("每天上午9点执行", "0 0 9 * * ?");
        examples.put("每周一上午9点执行", "0 0 9 ? * MON");
        examples.put("工作日上午9点执行", "0 0 9 ? * MON-FRI");
        examples.put("每月1号0点执行", "0 0 0 1 * ?");
        examples.put("每月最后一天0点执行", "0 0 0 L * ?");

        return Result.success(examples);
    }

    @Operation(summary = "Cron 表达式说明")
    @GetMapping("/help")
    public Result<Map<String, Object>> getCronHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("format", "秒 分 时 日 月 周");
        help.put("fields", Map.of(
                "秒", "0-59",
                "分", "0-59",
                "时", "0-23",
                "日", "1-31",
                "月", "1-12 或 JAN-DEC",
                "周", "0-7 或 SUN-SAT (0和7都代表周日)"
        ));

        help.put("wildcards", Map.of(
                "*", "匹配所有值",
                "?", "不指定值（日和周互斥）",
                "-", "范围，如 1-5",
                ",", "列举，如 1,3,5",
                "/", "步长，如 */5 表示每5个单位",
                "L", "最后，如 L 表示最后一天",
                "W", "工作日，如 5W 表示最近的工作日",
                "#", "第几个，如 2#3 表示第3个周二"
        ));

        return Result.success(help);
    }
}
