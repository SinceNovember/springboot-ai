package com.simple.ai.test.domain;

import java.util.Arrays;
import com.simple.ai.domain.agent.model.entity.ArmoryCommandEntity;
import com.simple.ai.domain.agent.model.entity.ExecuteCommandEntity;
import com.simple.ai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.simple.ai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.simple.ai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
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
 * Flow流程执行策略测试类
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/1/27 17:52
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowAgentExecuteTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private DefaultFlowAgentExecuteStrategyFactory defaultFlowAgentExecuteStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        String apply = armoryStrategyHandler.apply(
                ArmoryCommandEntity.builder()
                        .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commandIdList(Arrays.asList("2101", "2102", "2103"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext());

        ChatClient chatClient = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("2101"));
        log.info("客户端构建:{}", chatClient);
    }

    @Test
    public void testFlowAgentExecute() throws Exception {
        StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultFlowAgentExecuteStrategyFactory.armoryStrategyHandler();

        ExecuteCommandEntity executeCommandEntity = new ExecuteCommandEntity();
        executeCommandEntity.setAiAgentId("1");
        executeCommandEntity.setMessage("""
                搜索D盘下面有哪些根目录，并分析主要是用来装写什么的（只需要第一级目录即可，不需要子目录）
                """);
        executeCommandEntity.setSessionId("flow-session-id-" + System.currentTimeMillis());
        executeCommandEntity.setMaxStep(4);

        // 创建动态上下文
        DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext = new DefaultFlowAgentExecuteStrategyFactory.DynamicContext();
        dynamicContext.setMaxStep(executeCommandEntity.getMaxStep());
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setCurrentTask(executeCommandEntity.getMessage());

        String apply = executeHandler.apply(executeCommandEntity, dynamicContext);
        log.info("Flow执行结果:{}", apply);
    }

}