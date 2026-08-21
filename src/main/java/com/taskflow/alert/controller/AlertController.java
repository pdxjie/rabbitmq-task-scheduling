package com.taskflow.alert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.alert.service.AlertService;
import com.taskflow.common.Result;
import com.taskflow.entity.AlertRecord;
import com.taskflow.entity.AlertRule;
import com.taskflow.mapper.AlertRecordMapper;
import com.taskflow.mapper.AlertRuleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 告警管理控制器
 */
@Tag(name = "告警管理", description = "告警规则和通知管理")
@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertRecordMapper alertRecordMapper;

    @Operation(summary = "创建告警规则")
    @PostMapping("/rule")
    public Result<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        AlertRule created = alertService.createAlertRule(rule);
        return Result.success("告警规则创建成功", created);
    }

    @Operation(summary = "更新告警规则")
    @PutMapping("/rule/{id}")
    public Result<String> updateAlertRule(@PathVariable Long id, @RequestBody AlertRule rule) {
        rule.setId(id);
        alertRuleMapper.updateById(rule);
        return Result.success("告警规则更新成功");
    }

    @Operation(summary = "获取告警规则详情")
    @GetMapping("/rule/{id}")
    public Result<AlertRule> getAlertRule(@PathVariable Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        return rule != null ? Result.success(rule) : Result.error("告警规则不存在");
    }

    @Operation(summary = "获取告警规则列表")
    @GetMapping("/rule/list")
    public Result<List<AlertRule>> listAlertRules(
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Long clusterId) {

        LambdaQueryWrapper<AlertRule> wrapper = new LambdaQueryWrapper<>();
        if (ruleType != null) {
            wrapper.eq(AlertRule::getRuleType, ruleType);
        }
        if (clusterId != null) {
            wrapper.eq(AlertRule::getClusterId, clusterId);
        }
        wrapper.orderByDesc(AlertRule::getCreatedAt);

        return Result.success(alertRuleMapper.selectList(wrapper));
    }

    @Operation(summary = "删除告警规则")
    @DeleteMapping("/rule/{id}")
    public Result<String> deleteAlertRule(@PathVariable Long id) {
        alertRuleMapper.deleteById(id);
        return Result.success("告警规则删除成功");
    }

    @Operation(summary = "启用告警规则")
    @PostMapping("/rule/{id}/enable")
    public Result<String> enableAlertRule(@PathVariable Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setStatus("ENABLED");
            alertRuleMapper.updateById(rule);
            return Result.success("告警规则已启用");
        }
        return Result.error("告警规则不存在");
    }

    @Operation(summary = "禁用告警规则")
    @PostMapping("/rule/{id}/disable")
    public Result<String> disableAlertRule(@PathVariable Long id) {
        AlertRule rule = alertRuleMapper.selectById(id);
        if (rule != null) {
            rule.setStatus("DISABLED");
            alertRuleMapper.updateById(rule);
            return Result.success("告警规则已禁用");
        }
        return Result.error("告警规则不存在");
    }

    @Operation(summary = "获取告警记录")
    @GetMapping("/record/page")
    public Result<Page<AlertRecord>> pageAlertRecords(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status) {

        Page<AlertRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();

        if (level != null) {
            wrapper.eq(AlertRecord::getAlertLevel, level);
        }
        if (status != null) {
            wrapper.eq(AlertRecord::getStatus, status);
        }
        wrapper.orderByDesc(AlertRecord::getCreatedAt);

        return Result.success(alertRecordMapper.selectPage(page, wrapper));
    }

    @Operation(summary = "测试告警发送")
    @PostMapping("/test")
    public Result<String> testAlert(@RequestParam Long ruleId) {
        alertService.triggerAlert(
                ruleId,
                "【测试告警】系统测试",
                "这是一条测试告警消息\n\n发送时间: " + java.time.LocalDateTime.now(),
                "INFO"
        );
        return Result.success("测试告警已发送");
    }
}
