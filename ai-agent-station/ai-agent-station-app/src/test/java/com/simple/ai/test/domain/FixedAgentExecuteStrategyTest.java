package com.simple.ai.test.domain;

import com.simple.ai.domain.agent.model.entity.ExecuteCommandEntity;
import com.simple.ai.domain.agent.service.execute.fixed.FixedAgentExecuteStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/9/13 15:39
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FixedAgentExecuteStrategyTest {

    @Resource
    private FixedAgentExecuteStrategy fixedAgentExecuteStrategy;

    @Test
    public void test_execute() throws Exception {
        fixedAgentExecuteStrategy.execute(ExecuteCommandEntity.builder()
                .aiAgentId("6")
                .sessionId("10100101")
                .message(
                        """
                                       搜索D盘下面有哪些根目录，并分析主要是用来装写什么的（只需要第一级目录即可，不需要子目录）      
                                """
                ).build(), null);
    }

}
