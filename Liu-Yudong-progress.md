# Progress: create_session Referrer Policy

## 现象
调用 `http://127.0.0.1:8091/api/v1/create_session`，Chrome Network 显示
`Referrer Policy: strict-origin-when-cross-origin`。

## 结论
这不是真正的错误。Chrome 对每个请求都会展示 Referrer Policy。
真正失败原因是接口约定不匹配：

- 前端 `index.html` 用 **POST + JSON body**
- 后端原先是 **GET + @RequestBody**
- 浏览器地址栏 GET **不会带 JSON body**
- POST 打到仅 GET 的接口 → **405**；GET 无 body → **400**
- 失败请求旁就会看到那行 Referrer Policy

## 修复
`AgentServiceController#create_session`：
- POST `/api/v1/create_session` + JSON `{agentId, userId}`（页面用法）
- GET `/api/v1/create_session?agentId=100001&userId=admin`（地址栏/curl）
- CORS 显式允许 GET/POST/OPTIONS

## 验证
重启后端后：
- 页面发消息应能创建会话
- 浏览器打开：`http://127.0.0.1:8091/api/v1/create_session?agentId=100001&userId=admin`

## 状态
代码已改，需重启 `ai-agent-scaffold-lite-app`。
