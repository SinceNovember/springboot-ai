package com.simple.ai.api;


import java.util.List;
import com.simple.ai.api.dto.AiAgentConfigResponseDTO;
import com.simple.ai.api.dto.ChatRequestDTO;
import com.simple.ai.api.dto.ChatResponseDTO;
import com.simple.ai.api.dto.CreateSessionRequestDTO;
import com.simple.ai.api.dto.CreateSessionResponseDTO;
import com.simple.ai.api.response.Response;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * 智能体服务接口
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/1/20 08:16
 */
public interface IAgentService {

    Response<List<AiAgentConfigResponseDTO>> queryAiAgentConfigList();

    Response<CreateSessionResponseDTO> createSession(CreateSessionRequestDTO requestDTO);

    Response<ChatResponseDTO> chat(ChatRequestDTO requestDTO);

    ResponseBodyEmitter chatStream(ChatRequestDTO requestDTO);

}
