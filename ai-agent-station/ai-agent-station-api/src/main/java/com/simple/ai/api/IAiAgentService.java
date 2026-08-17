package com.simple.ai.api;

import java.util.List;
import com.simple.ai.api.dto.AiAgentResponseDTO;
import com.simple.ai.api.dto.ArmoryAgentRequestDTO;
import com.simple.ai.api.dto.ArmoryApiRequestDTO;
import com.simple.ai.api.dto.AutoAgentRequestDTO;
import com.simple.ai.api.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * Ai Agent 服务接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/8/7 17:52
 */
public interface IAiAgentService {

    ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request, HttpServletResponse response);

    /**
     * 装配智能体
     */
    Response<Boolean> armoryAgent(ArmoryAgentRequestDTO request);

    /**
     * 查询可用的智能体列表
     */
    Response<List<AiAgentResponseDTO>> queryAvailableAgents();

    /**
     * 装配API
     */
    Response<Boolean> armoryApi(ArmoryApiRequestDTO request);

}
