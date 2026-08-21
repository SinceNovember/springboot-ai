package cn.bugstack.ai.test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import cn.bugstack.ai.trigger.http.AgentServiceController;
import com.simple.ai.api.dto.ChatResponseDTO;
import org.junit.Assert;
import org.junit.Test;

/**
 * 临时验证：控制器能否从智能体的多条输出里正确提取出前端可用的响应。
 */
public class ChatResponseResolveTest {

    private static final String XML = "<mxfile host=\\\"app.diagrams.net\\\">\n  <diagram id=\\\"registration_flow\\\" name=\\\"注册流程图\\\">\n    <mxGraphModel><root><mxCell id=\\\"0\\\" /></root></mxGraphModel>\n  </diagram>\n</mxfile>";

    private ChatResponseDTO resolve(List<String> messages) throws Exception {
        Method method = AgentServiceController.class.getDeclaredMethod("resolveChatResponse", List.class);
        method.setAccessible(true);
        return (ChatResponseDTO) method.invoke(new AgentServiceController(), messages);
    }

    @Test
    public void test_审查说明加json围栏() throws Exception {
        String reviewer = "已完成对注册流程图 XML 的检查。审查结论如下：\n\n"
            + "**✅ XML 语法检查**：通过。\n\nXML 无需修正，以下为最终输出：\n\n"
            + "```json\n{\"type\": \"drawio\", \"content\": \"" + XML + "\"}\n```";

        ChatResponseDTO dto = resolve(Arrays.asList(
            "## 需求分析结果\n### 用户意图\n用户希望实现一个简单的注册流程图。",
            "# 注册流程图 - Draw.io XML\n```xml\n<mxfile host=\"a\"><diagram>d</diagram></mxfile>\n```",
            reviewer));

        Assert.assertEquals("drawio", dto.getType());
        Assert.assertTrue(dto.getContent().startsWith("<mxfile"));
        Assert.assertTrue(dto.getContent().endsWith("</mxfile>"));
        Assert.assertTrue(dto.getContent().contains("registration_flow"));
    }

    @Test
    public void test_严格纯json() throws Exception {
        ChatResponseDTO dto = resolve(Arrays.asList("分析", "绘图",
            "{\"type\": \"drawio\", \"content\": \"" + XML + "\"}"));

        Assert.assertEquals("drawio", dto.getType());
        Assert.assertTrue(dto.getContent().startsWith("<mxfile"));
    }

    @Test
    public void test_只有xml围栏没有json() throws Exception {
        ChatResponseDTO dto = resolve(Arrays.asList("分析",
            "这是生成的图：\n```xml\n<mxfile host=\"x\"><diagram>d</diagram></mxfile>\n```\n请查看。"));

        Assert.assertEquals("drawio", dto.getType());
        Assert.assertEquals("<mxfile host=\"x\"><diagram>d</diagram></mxfile>", dto.getContent());
    }

    @Test
    public void test_追问用户补充信息() throws Exception {
        ChatResponseDTO dto = resolve(Arrays.asList(
            "{\"type\": \"user\", \"content\": \"请补充关于节点的具体信息\"}"));

        Assert.assertEquals("user", dto.getType());
        Assert.assertEquals("请补充关于节点的具体信息", dto.getContent());
    }

    @Test
    public void test_无结构化结果时只返回最后一条() throws Exception {
        ChatResponseDTO dto = resolve(Arrays.asList("第一条中间产物", "我不知道该画什么"));

        Assert.assertEquals("user", dto.getType());
        Assert.assertEquals("我不知道该画什么", dto.getContent());
    }

    @Test
    public void test_空输入不抛异常() throws Exception {
        ChatResponseDTO dto = resolve(Arrays.asList());

        Assert.assertEquals("user", dto.getType());
        Assert.assertEquals("", dto.getContent());
    }
}
