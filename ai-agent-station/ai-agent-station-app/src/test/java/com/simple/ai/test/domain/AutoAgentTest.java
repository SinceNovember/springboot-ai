package com.simple.ai.test.domain;

import java.util.Arrays;
import com.simple.ai.domain.agent.model.entity.ArmoryCommandEntity;
import com.simple.ai.domain.agent.model.entity.ExecuteCommandEntity;
import com.simple.ai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.simple.ai.domain.agent.service.armory.node.factory.DefaultArmoryStrategyFactory;
import com.simple.ai.domain.agent.service.execute.auto.step.factory.DefaultAutoAgentExecuteStrategyFactory;
import com.simple.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/7/27 17:52
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoAgentTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private DefaultAutoAgentExecuteStrategyFactory defaultAutoAgentExecuteStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        String apply = armoryStrategyHandler.apply(
                ArmoryCommandEntity.builder()
                        .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commandIdList(Arrays.asList("3101", "3102", "3103"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext());

        ChatClient chatClient = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3101"));
        log.info("客户端构建:{}", chatClient);
    }

    @Test
    public void autoAgent() throws Exception {
        StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

        ExecuteCommandEntity executeCommandEntity = new ExecuteCommandEntity();
        executeCommandEntity.setAiAgentId("3");
        executeCommandEntity.setMessage("搜索D盘下面有哪些根目录，并分析主要是用来装写什么的（只需要第一级目录即可，不需要子目录）");
        executeCommandEntity.setSessionId("session-id-" + System.currentTimeMillis());
        executeCommandEntity.setMaxStep(3);

        String apply = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        log.info("测试结果:{}", apply);
    }

}
