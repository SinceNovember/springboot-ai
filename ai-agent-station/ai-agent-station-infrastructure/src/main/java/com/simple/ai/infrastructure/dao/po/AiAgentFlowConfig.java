package com.simple.ai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体-客户端关联表
 * @author bugstack虫洞栈
 * @description 智能体-客户端关联表 PO 对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentFlowConfig {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 智能体ID
     */
    private Long agentId;

    /**
     * 客户端ID
     */
    private Long clientId;

    /**
     * 序列号(执行顺序)
     */
    private Integer sequence;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}