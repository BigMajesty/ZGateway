package com.study.gateway.config.center.api;

import com.study.common.rule.Rule;

import java.util.List;

/**
 * 规则变化监听器
 */
public interface IRulesChangeListener {
    void onRulesChange(List<Rule> ruleList);
}
