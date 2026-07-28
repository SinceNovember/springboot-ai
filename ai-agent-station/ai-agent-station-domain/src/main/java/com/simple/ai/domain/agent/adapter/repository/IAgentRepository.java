package com.simple.ai.domain.agent.adapter.repository;

import java.util.List;
import com.simple.ai.domain.agent.model.valobj.AiClientAdvisorVO;
import com.simple.ai.domain.agent.model.valobj.AiClientApiVO;
import com.simple.ai.domain.agent.model.valobj.AiClientModelVO;
import com.simple.ai.domain.agent.model.valobj.AiClientSystemPromptVO;
import com.simple.ai.domain.agent.model.valobj.AiClientToolMcpVO;
import com.simple.ai.domain.agent.model.valobj.AiClientVO;

/**
 * AiAgent 仓储接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/6/27 16:48
 */
public interface IAgentRepository {

    List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIdList);

    List<AiClientModelVO> AiClientModelVOByClientIds(List<String> clientIdList);

    List<AiClientToolMcpVO> AiClientToolMcpVOByClientIds(List<String> clientIdList);

    List<AiClientSystemPromptVO> AiClientSystemPromptVOByClientIds(List<String> clientIdList);

    List<AiClientAdvisorVO> AiClientAdvisorVOByClientIds(List<String> clientIdList);

    List<AiClientVO> AiClientVOByClientIds(List<String> clientIdList);

    List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIdList);

    List<AiClientModelVO> AiClientModelVOByModelIds(List<String> modelIdList);

}
