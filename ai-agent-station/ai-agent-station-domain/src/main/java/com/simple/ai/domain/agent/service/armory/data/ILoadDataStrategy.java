package com.simple.ai.domain.agent.service.armory.data;

import com.simple.ai.domain.agent.model.entity.ArmoryCommandEntity;
import com.simple.ai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

/**
 * 数据加载策略
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 17:16
 */
public interface ILoadDataStrategy {

    void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext);

}
