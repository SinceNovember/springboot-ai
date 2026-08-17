package com.simple.ai.api;

import java.util.List;
import com.simple.ai.api.dto.AiAgentDrawConfigQueryRequestDTO;
import com.simple.ai.api.dto.AiAgentDrawConfigRequestDTO;
import com.simple.ai.api.dto.AiAgentDrawConfigResponseDTO;
import com.simple.ai.api.response.Response;

/**
 * AI智能体拖拉拽配置管理服务接口
 *
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/9/28 07:35
 */
public interface IAiAgentDrawAdminService {

    /**
     * 保存拖拉拽流程图配置
     *
     * @param request 配置请求参数
     * @return 保存结果
     */
    Response<String> saveDrawConfig(AiAgentDrawConfigRequestDTO request);

    /**
     * 获取拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 配置数据
     */
    Response<AiAgentDrawConfigResponseDTO> getDrawConfig(String configId);

    /**
     * 分页查询拖拉拽流程图配置列表
     *
     * @param request 查询条件与分页参数
     * @return 配置列表
     */
    Response<List<AiAgentDrawConfigResponseDTO>> queryDrawConfigList(AiAgentDrawConfigQueryRequestDTO request);

    /**
     * 删除拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    Response<String> deleteDrawConfig(String configId);

}
