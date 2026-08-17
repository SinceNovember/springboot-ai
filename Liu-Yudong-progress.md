# Progress: validate-login 404

## 现象
前端调用 `http://127.0.0.1:8099/api/v1/admin/admin-user/validate-login` 失败。
浏览器 Network 里的 `Referrer Policy` **不是错误**。

## 根因
后端 `application-dev.yml` 配置了 `context-path: /ai-agent-station`，
前端 `api.ts` 的 `BASE_DOMAIN` 未带此前缀 → 实际 404。

验证：
- 无前缀 → 404
- 有前缀 `.../ai-agent-station/api/v1/admin/admin-user/validate-login` → 200（业务码 0001 为账号/密码或库数据问题）

## 最终方案（按用户要求）
- 删除后端 `server.servlet.context-path: /ai-agent-station`（dev/prod）
- 前端保持原样：`BASE_DOMAIN=http://127.0.0.1:8099`（已还原先前改动）
- 接口地址：`http://127.0.0.1:8099/api/v1/admin/admin-user/validate-login`

## 状态
需重启后端使配置生效。
