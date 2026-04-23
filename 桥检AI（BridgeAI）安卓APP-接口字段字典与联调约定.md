# 桥检AI（BridgeAI）安卓APP 接口字段字典与联调约定

## 1. 文档目标

本文件用于补齐接口级字段字典，解决联调时最常见的3类问题：

1. 前后端字段名理解不一致
2. 枚举值含义不一致
3. 页面状态依赖字段不明确

---

## 2. 联调总原则

1. 字段命名以接口文档为准，不再各自发挥。
2. 枚举值只允许使用约定值，不做“中文+英文混用”。
3. 所有 ID 字段必须明确“本地ID”还是“服务端ID”。
4. 页面判断状态优先使用显式字段，不依赖前端猜测。

---

## 3. ID 字段约定

| 字段 | 含义 | 来源 |
| --- | --- | --- |
| id | 服务端主键ID | 服务端生成 |
| localId | 安卓Room本地主键 | 客户端生成 |
| clientRecordId | 客户端检测记录幂等ID | 客户端生成 |
| clientMediaId | 客户端媒体幂等ID | 客户端生成 |
| reportCode | 服务端报告编号 | 服务端生成 |
| taskNo | 服务端AI任务号 | 服务端生成 |

联调要求：

1. 安卓上传时必须带 `clientRecordId`
2. 媒体上传时必须带 `clientMediaId`
3. 服务端返回后安卓要回填 `id`

---

## 4. 枚举字典

## 4.1 role

| 值 | 含义 |
| --- | --- |
| admin | 管理员 |
| leader | 组长/项目负责人 |
| inspector | 检测员 |

## 4.2 projectType

| 值 | 含义 |
| --- | --- |
| 定期检测 | 周期性桥梁检测 |
| 特殊检测 | 针对特定问题检测 |
| 应急检测 | 紧急异常场景检测 |

## 4.3 projectStatus

| 值 | 含义 | 页面影响 |
| --- | --- | --- |
| draft | 草稿/未正式开始 | 任务列表显示待开始 |
| in_progress | 进行中 | 可进入检测 |
| completed | 已完成 | 可生成或查看报告 |

## 4.4 componentCategory

| 值 | 含义 |
| --- | --- |
| 上部结构 | 上部结构构件 |
| 下部结构 | 下部结构构件 |
| 附属设施 | 附属设施构件 |

## 4.5 planStatus

| 值 | 含义 |
| --- | --- |
| pending | 未开始 |
| processing | 处理中 |
| done | 已完成 |

## 4.6 defectType

MVP统一值：

1. 裂缝
2. 混凝土剥落/露筋
3. 锈蚀
4. 混凝土空洞
5. 表面劣化
6. 渗水/潮湿
7. 收缩裂缝
8. 施工缺陷
9. 无病害

注意：

1. 安卓端表单选项与后端字典必须严格一致。
2. 若算法输出值不同，需在服务端或客户端统一映射，不允许直接透传脏枚举。

## 4.7 defectLevel

| 值 | 含义 |
| --- | --- |
| 轻微 | 轻微病害 |
| 中等 | 中等病害 |
| 严重 | 严重病害 |
| 无病害 | 明确未发现病害 |

## 4.8 aiStatus

| 值 | 含义 | 页面影响 |
| --- | --- | --- |
| none | 尚未触发AI | 构件状态通常为已采集 |
| pending | 等待识别/识别中 | 显示识别中 |
| done | 识别完成 | 显示AI结果 |
| failed | 识别失败 | 显示失败并允许重试 |

## 4.9 finalStatus

| 值 | 含义 |
| --- | --- |
| drafted | 已保存但未最终确认 |
| confirmed | 已人工确认 |

## 4.10 syncStatus

| 值 | 含义 |
| --- | --- |
| pending | 待同步 |
| syncing | 同步中 |
| synced | 已同步 |
| failed | 同步失败 |

## 4.11 reportStatus

| 值 | 含义 | 页面影响 |
| --- | --- | --- |
| draft | 草稿 | 可编辑 |
| completed | 已完成 | 可导出PDF |
| reported | 已上报 | 只读为主 |

---

## 5. 核心对象字段字典

## 5.1 Bridge 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 是 | 桥梁ID |
| bridge_code | String | 是 | 桥梁唯一编码 |
| bridge_name | String | 是 | 桥梁名称 |
| route | String | 否 | 所属路线 |
| bridge_type | String | 是 | 桥梁类型 |
| structure_type | String | 是 | 结构形式 |
| total_length | Double | 否 | 全长，单位米 |
| bridge_width | Double | 否 | 桥宽，单位米 |
| main_span | Double | 否 | 主跨，单位米 |
| construction_year | Int | 是 | 建成年份 |
| address | String | 否 | 地址描述 |
| maintenance_unit | String | 否 | 养护单位 |

## 5.2 Project 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 是 | 项目ID |
| project_code | String | 是 | 项目编号 |
| project_name | String | 是 | 项目名称 |
| bridge_id | Long | 是 | 关联桥梁 |
| project_type | String | 是 | 项目类型 |
| project_status | String | 是 | 项目状态 |
| leader_id | Long | 是 | 负责人 |
| start_date | String | 否 | 开始日期 |
| end_date | String | 否 | 结束日期 |

## 5.3 InspectionPlan 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 是 | 计划ID |
| project_id | Long | 是 | 项目ID |
| component_category | String | 是 | 构件分类 |
| component_type | String | 是 | 构件类型 |
| component_number | String | 是 | 构件编号 |
| defect_types | Array<String> | 否 | 重点病害列表 |
| assigned_inspector_id | Long | 否 | 指派检测员 |
| plan_status | String | 是 | 计划状态 |

## 5.4 InspectionRecord 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 否 | 服务端记录ID |
| client_record_id | String | 是 | 客户端幂等ID |
| project_id | Long | 是 | 项目ID |
| plan_id | Long | 否 | 计划ID |
| bridge_id | Long | 是 | 桥梁ID |
| inspector_id | Long | 是 | 检测员ID |
| component_category | String | 是 | 构件分类 |
| component_type | String | 是 | 构件类型 |
| component_number | String | 是 | 构件编号 |
| defect_type | String | 是 | 病害类型 |
| defect_level | String | 是 | 病害等级 |
| location_desc | String | 否 | 位置描述 |
| size_params | Object | 否 | 尺寸参数 |
| ai_status | String | 是 | AI状态 |
| ai_result | Object | 否 | AI原始结果 |
| final_status | String | 是 | 最终确认状态 |
| remarks | String | 否 | 备注 |
| longitude | Double | 否 | 经度 |
| latitude | Double | 否 | 纬度 |
| inspection_time | String | 否 | 检测时间 |
| sync_status | String | 否 | 仅客户端本地使用，服务端可不返回 |

## 5.5 InspectionMedia 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 否 | 服务端媒体ID |
| client_media_id | String | 是 | 客户端媒体幂等ID |
| record_id | Long | 是 | 所属记录ID |
| media_type | String | 是 | photo/video |
| file_url | String | 否 | 远端文件地址 |
| thumbnail_url | String | 否 | 缩略图地址 |
| mime_type | String | 否 | MIME类型 |
| width | Int | 否 | 宽 |
| height | Int | 否 | 高 |
| duration_ms | Long | 否 | 视频时长 |
| sort_order | Int | 否 | 排序 |

## 5.6 AiTask 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| task_no | String | 是 | AI任务号 |
| record_id | Long | 是 | 记录ID |
| task_status | String | 是 | pending/running/success/failed |
| engine_type | String | 是 | mock/remote/local |
| model_version | String | 否 | 模型版本 |
| result | Object | 否 | AI识别结果 |
| error_message | String | 否 | 失败原因 |

## 5.7 Report 字段字典

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 否 | 报告ID |
| report_code | String | 否 | 报告编号 |
| project_id | Long | 是 | 项目ID |
| bridge_id | Long | 是 | 桥梁ID |
| report_status | String | 是 | 报告状态 |
| report_content | Object | 是 | 报告完整结构化内容 |
| technical_rating | Object | 否 | 技术状况评定 |
| maintenance_advice | Object | 否 | 养护建议 |
| signers | Object | 否 | 签字信息 |
| pdf_url | String | 否 | PDF地址 |

---

## 6. 页面与字段依赖关系

## 6.1 任务列表页

依赖最关键字段：

1. `project_status`
2. `project_name`
3. `bridge_name`
4. `leader_id`
5. `completed_components`
6. `total_components`
7. `has_unsynced_data`

## 6.2 项目检测页

依赖最关键字段：

1. `component_category`
2. `component_type`
3. `component_number`
4. `plan_status`
5. `ai_status`
6. `final_status`

## 6.3 构件结果页

依赖最关键字段：

1. `defect_type`
2. `defect_level`
3. `location_desc`
4. `size_params`
5. `ai_result`
6. `media_list`

## 6.4 报告页

依赖最关键字段：

1. `report_status`
2. `report_content`
3. `pdf_url`

---

## 7. 请求字段约定

## 7.1 创建类接口

规则：

1. 客户端提交时必须带必要业务字段
2. 服务端生成主键和编号类字段

例：

1. 创建记录时不传 `id`
2. 必传 `client_record_id`

## 7.2 更新类接口

规则：

1. 优先传完整对象，减少局部字段歧义
2. 状态变更接口可单独使用 PATCH

## 7.3 列表类接口

规则：

1. 所有列表接口支持分页
2. 返回 `list/page/page_size/total`

---

## 8. 响应字段约定

## 8.1 空字段处理

规则：

1. 可选对象字段允许为 `null`
2. 数组字段优先返回 `[]` 而不是 `null`

## 8.2 布尔字段

优先使用：

1. `true/false`

不建议：

1. `"Y"/"N"`
2. `1/0` 混用

---

## 9. 联调约定

## 9.1 联调顺序

建议顺序：

1. 认证
2. 桥梁
3. 项目
4. 计划
5. 检测记录
6. 媒体
7. AI
8. 报告

## 9.2 联调检查点

每个接口联调时至少确认：

1. 字段名是否一致
2. 可选字段是否稳定返回
3. 枚举值是否一致
4. 错误码是否一致
5. 安卓本地解析是否正常

## 9.3 Mock数据要求

后端在正式联调前应提供：

1. 至少3座桥梁
2. 至少2个项目
3. 至少10条构件计划
4. 至少5条检测记录
5. 至少2份报告

---

## 10. 开始编码前必须冻结的字段

这批字段建议视为“高风险变更字段”，除非必要，不再调整：

1. `client_record_id`
2. `client_media_id`
3. `project_status`
4. `plan_status`
5. `ai_status`
6. `final_status`
7. `report_status`
8. `component_category`
9. `component_type`
10. `component_number`
11. `defect_type`
12. `defect_level`

---

## 11. 本文结论

桥检AI联调真正难的不是接口数量，而是字段口径。

只要字段字典、枚举值、状态语义统一，安卓端、本地数据库、后端服务和测试用例就能在同一个坐标系里工作。
