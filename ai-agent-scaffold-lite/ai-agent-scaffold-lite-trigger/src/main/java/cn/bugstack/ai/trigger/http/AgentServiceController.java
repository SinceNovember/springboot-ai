package cn.bugstack.ai.trigger.http;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import cn.bugstack.ai.domain.agent.model.valobj.AiAgentConfigTableVO;
import cn.bugstack.ai.domain.agent.service.IChatService;
import cn.bugstack.ai.types.enums.ResponseCode;
import cn.bugstack.ai.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.simple.ai.api.IAgentService;
import com.simple.ai.api.dto.AiAgentConfigResponseDTO;
import com.simple.ai.api.dto.ChatRequestDTO;
import com.simple.ai.api.dto.ChatResponseDTO;
import com.simple.ai.api.dto.CreateSessionRequestDTO;
import com.simple.ai.api.dto.CreateSessionResponseDTO;
import com.simple.ai.api.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2026/1/20 08:23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS
})
public class AgentServiceController implements IAgentService {

    private static final String TYPE_DRAWIO = "drawio";
    private static final String TYPE_USER = "user";

    /** markdown 代码块里的 JSON，形如 ```json {...} ``` */
    private static final Pattern JSON_FENCE_PATTERN = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?})\\s*```");

    /** 裸的 draw.io XML */
    private static final Pattern MX_FILE_PATTERN = Pattern.compile("<mxfile[\\s\\S]*?</mxfile>");

    @Resource
    private IChatService chatService;

    @RequestMapping(value = "query_ai_agent_config_list", method = RequestMethod.GET)
    @Override
    public Response<List<AiAgentConfigResponseDTO>> queryAiAgentConfigList() {
        try {
            log.info("查询智能体配置列表");

            List<AiAgentConfigTableVO.Agent> agentConfigs = chatService.queryAiAgentConfigList();

            List<AiAgentConfigResponseDTO> responseDTOS = agentConfigs.stream().map(agentConfig -> {
                AiAgentConfigResponseDTO responseDTO = new AiAgentConfigResponseDTO();
                responseDTO.setAgentId(agentConfig.getAgentId());
                responseDTO.setAgentName(agentConfig.getAgentName());
                responseDTO.setAgentDesc(agentConfig.getAgentDesc());
                return responseDTO;
            }).collect(Collectors.toList());

            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTOS)
                    .build();

        } catch (AppException e) {
            log.error("查询智能体配置列表异常", e);
            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询智能体配置列表失败", e);
            return Response.<List<AiAgentConfigResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "create_session", method = RequestMethod.POST)
    @Override
    public Response<CreateSessionResponseDTO> createSession(@RequestBody CreateSessionRequestDTO requestDTO) {
        return doCreateSession(requestDTO);
    }

    /**
     * 浏览器地址栏 / curl 手工验证：GET 不能可靠携带 JSON body，改用 query。
     */
    @RequestMapping(value = "create_session", method = RequestMethod.GET)
    public Response<CreateSessionResponseDTO> createSessionByQuery(
            @RequestParam("agentId") String agentId,
            @RequestParam(value = "userId", required = false, defaultValue = "admin") String userId) {
        CreateSessionRequestDTO requestDTO = new CreateSessionRequestDTO();
        requestDTO.setAgentId(agentId);
        requestDTO.setUserId(userId);
        return doCreateSession(requestDTO);
    }

    private Response<CreateSessionResponseDTO> doCreateSession(CreateSessionRequestDTO requestDTO) {
        String agentId = requestDTO == null ? null : requestDTO.getAgentId();
        String userId = requestDTO == null ? null : requestDTO.getUserId();
        try {
            log.info("创建会话 agentId:{} userId:{}", agentId, userId);
            String sessionId = chatService.createSession(agentId, userId);

            CreateSessionResponseDTO responseDTO = new CreateSessionResponseDTO();
            responseDTO.setSessionId(sessionId);

            return Response.<CreateSessionResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseDTO)
                    .build();
        } catch (AppException e) {
            log.error("创建会话异常 agentId:{} userId:{}", agentId, userId, e);
            return Response.<CreateSessionResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("创建会话失败 agentId:{} userId:{}", agentId, userId, e);
            return Response.<CreateSessionResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "chat", method = RequestMethod.POST)
    @Override
    public Response<ChatResponseDTO> chat(@RequestBody ChatRequestDTO requestDTO) {
        try {
            log.info("智能体对话 agentId:{} userId:{}", requestDTO.getAgentId(), requestDTO.getUserId());
            String sessionId = requestDTO.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = chatService.createSession(requestDTO.getAgentId(), requestDTO.getUserId());
            }

            List<String> messages = chatService.handleMessage(requestDTO.getAgentId(), requestDTO.getUserId(), sessionId, requestDTO.getMessage());

            ChatResponseDTO responseDTO = resolveChatResponse(messages);

            return Response.<ChatResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(responseDTO)
                .build();
        } catch (AppException e) {
            log.error("智能体对话异常", e);
            return Response.<ChatResponseDTO>builder()
                .code(e.getCode())
                .info(e.getInfo())
                .build();
        } catch (Exception e) {
            log.error("智能体对话败 agentId:{} userId:{}", requestDTO.getAgentId(), requestDTO.getUserId(), e);
            return Response.<ChatResponseDTO>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ResponseCode.UN_ERROR.getInfo())
                .build();
        }
    }

    @RequestMapping(value = "chat_stream", method = RequestMethod.POST)
    @Override
    public ResponseBodyEmitter chatStream(@RequestBody ChatRequestDTO requestDTO) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        try {
            log.info("流式对话 agentId:{} userId:{} sessionId:{} message:{}", requestDTO.getAgentId(), requestDTO.getUserId(), requestDTO.getSessionId(), requestDTO.getMessage());
            chatService.handleMessageStream(requestDTO.getAgentId(), requestDTO.getUserId(), requestDTO.getSessionId(), requestDTO.getMessage())
                    .subscribe(
                            event -> {
                                try {
                                    emitter.send(event.stringifyContent());
                                } catch (Exception e) {
                                    log.error("流式对话发送失败", e);
                                    emitter.completeWithError(e);
                                }
                            },
                            emitter::completeWithError,
                            emitter::complete
                    );
        } catch (Exception e) {
            log.error("流式对话失败", e);
            emitter.completeWithError(e);
        }
        return emitter;
    }


    /**
     * 从智能体的多条输出里解析出前端可用的响应。
     * <p>
     * 串行工作流每个 Agent 都会产出一条消息，只有最后一条是约定的 JSON 结果。但模型经常不严格听话，
     * 会在 JSON 外面套一层说明文字或 markdown 代码块，所以这里从后往前逐条尝试，按可靠度依次降级：
     * 结构化 JSON -> 裸 mxfile XML -> 纯文本。
     */
    private ChatResponseDTO resolveChatResponse(List<String> messages) {
        if (null == messages || messages.isEmpty()) {
            return textResponse("");
        }

        for (int i = messages.size() - 1; i >= 0; i--) {
            String message = messages.get(i);
            if (isBlank(message)) {
                continue;
            }

            // 1. 约定的 {"type":..,"content":..}，允许被 markdown 代码块或说明文字包裹
            ChatResponseDTO structured = parseStructured(message);
            if (null != structured) {
                return structured;
            }

            // 2. 模型没按 JSON 输出时，直接从文本里捞 draw.io XML
            Matcher matcher = MX_FILE_PATTERN.matcher(message);
            if (matcher.find()) {
                log.info("智能体未按 JSON 输出，已从文本中提取 mxfile");
                ChatResponseDTO responseDTO = new ChatResponseDTO();
                responseDTO.setType(TYPE_DRAWIO);
                responseDTO.setContent(matcher.group());
                return responseDTO;
            }
        }

        // 3. 兜底：只返回最后一条消息，不要把所有 Agent 的中间产物拼给前端
        log.warn("智能体输出中未找到结构化结果或 mxfile，降级为纯文本返回");
        return textResponse(messages.get(messages.size() - 1));
    }

    /**
     * 依次尝试：markdown 代码块里的 JSON -> 整条消息 -> 首个 { 到末个 } 之间的片段。
     * 解析成功且 content 非空才算有效，否则返回 null 交给上层继续降级。
     */
    private ChatResponseDTO parseStructured(String message) {
        List<String> candidates = new ArrayList<>();

        Matcher fenced = JSON_FENCE_PATTERN.matcher(message);
        while (fenced.find()) {
            candidates.add(fenced.group(1));
        }

        candidates.add(message.trim());

        int begin = message.indexOf('{');
        int end = message.lastIndexOf('}');
        if (begin >= 0 && end > begin) {
            candidates.add(message.substring(begin, end + 1));
        }

        for (String candidate : candidates) {
            ChatResponseDTO responseDTO = tryParse(candidate);
            if (null != responseDTO) {
                return responseDTO;
            }
        }

        return null;
    }

    private ChatResponseDTO tryParse(String candidate) {
        if (isBlank(candidate) || '{' != candidate.trim().charAt(0)) {
            return null;
        }
        try {
            ChatResponseDTO responseDTO = JSON.parseObject(candidate, ChatResponseDTO.class);
            if (null == responseDTO || isBlank(responseDTO.getContent())) {
                return null;
            }
            if (isBlank(responseDTO.getType())) {
                responseDTO.setType(TYPE_USER);
            }
            return responseDTO;
        } catch (Exception ignore) {
            return null;
        }
    }

    private ChatResponseDTO textResponse(String content) {
        ChatResponseDTO responseDTO = new ChatResponseDTO();
        responseDTO.setType(TYPE_USER);
        responseDTO.setContent(content);
        return responseDTO;
    }

    private boolean isBlank(String value) {
        return null == value || value.trim().isEmpty();
    }

}
