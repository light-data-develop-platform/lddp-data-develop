DELETE FROM dict WHERE type in (30,17);

INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('SYNC', '', '{"type":"object","children":[{"type":"object","title":"数据来源","name":"sourceMap","children":[{"type":"number","title":"同步模式","name":"syncModel","noStyle":true},{"type":"number","title":"类型","name":"type","noStyle":true,"bind":{"field":"sourceMap.sourceId","transformer":"{{optionCollections.sourceMap_sourceId#find.type}}"}},{"type":"number","title":"数据源","widget":"select","name":"sourceId","required":true,"props":{"placeholder":"请选择数据源","optionsFromRequest":true,"name":"sourceMap_sourceId","method":"get","url":"/taier/api/dataSource/manager/queryByTenantId","transformer":"sourceId"}},{"type":"number","title":"schema","widget":"select","name":"schema","props":{"placeholder":"请选择 schema","optionsFromRequest":true,"name":"sourcemap_schema","method":"post","url":"/taier/api/dataSource/addDs/getAllSchemas","params":{"sourceId":"{{form#sourceMap.sourceId}}"},"required":["sourceId"],"transformer":"table"},"depends":["sourceMap.sourceId"],"hidden":[{"field":"form.sourceMap.type","value":"2,4","isNot":true}]},{"type":"string","title":"表名","name":"table","widget":"SelectWithPreviewer","required":true,"props":{"placeholder":"请选择表名","optionsFromRequest":true,"name":"sourcemap_table","method":"post","url":"/taier/api/dataSource/addDs/tablelist","params":{"sourceId":"{{form#sourceMap.sourceId}}","schema":"{{form#sourceMap.schema}}","isSys":false,"isRead":true},"required":["sourceId"],"transformer":"table"},"depends":["sourceMap.sourceId","sourceMap.schema"],"hidden":[{"field":"form.sourceMap.type","value":"1,2,3,4,7,8,27,45,50","isNot":true}]},{"type":"string","title":"增量标识字段","name":"increColumn","widget":"select","required":true,"props":{"placeholder":"请选择增量标识字段","optionsFromRequest":true,"name":"sourcemap_increColumn","method":"post","url":"/taier/api/task/getIncreColumn","params":{"sourceId":"{{form#sourceMap.sourceId}}","tableName":"{{form#sourceMap.table}}","schema":"{{form#sourceMap.schema}}"},"required":["sourceId","tableName"],"transformer":"incrementColumn"},"depends":["sourceMap.table"],"hidden":[{"field":"form.sourceMap.type","value":"1,2,3,4","isNot":true},{"field":"form.sourceMap.syncModel","value":"1","isNot":true}]},{"type":"string","title":"编码","name":"encoding","widget":"select","required":true,"initialValue":"utf-8","props":{"placeholder":"请选择编码","options":[{"label":"utf-8","value":"utf-8"},{"label":"gdb","value":"gdb"}]},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"开始行健","name":"startRowkey","props":{"placeholder":"请输入开始行健"},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"结束行健","name":"endRowkey","props":{"placeholder":"请输入结束行健"},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"行健二进制转换","name":"isBinaryRowkey","widget":"radio","initialValue":"0","props":{"options":[{"label":"FALSE","value":"0"},{"label":"TRUE","value":"1"}]},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"每次RPC请求获取行数","name":"scanCacheSize","widget":"inputNumber","props":{"placeholder":"请输入大小, 默认为256","min":0,"suffix":"行"},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"每次RPC请求获取列数","name":"scanBatchSize","widget":"inputNumber","props":{"placeholder":"请输入大小, 默认为100","min":0,"suffix":"列"},"hidden":[{"field":"form.sourceMap.type","value":"8","isNot":true}]},{"type":"string","title":"数据过滤","name":"where","widget":"textarea","rules":[{"max":1000,"message":"过滤语句不可超过1000个字符!"}],"props":{"placeholder":"请参考相关SQL语法填写where过滤语句（不要填写where关键字）。该过滤语句通常用作增量同步","autoSize":{"minRows":2,"maxRows":6}},"hidden":[{"field":"form.sourceMap.type","value":"1,2,3,4","isNot":true}]},{"type":"string","title":"切分键","name":"split","widget":"select","props":{"placeholder":"请选择切分键","optionsFromRequest":true,"name":"sourcemap_split","method":"post","url":"/taier/api/dataSource/addDs/columnForSyncopate","params":{"sourceId":"{{form#sourceMap.sourceId}}","tableName":"{{form#sourceMap.table}}","schema":"{{form#sourceMap.schema}}"},"required":["sourceId","tableName"],"transformer":"split"},"depends":["sourceMap.table"],"hidden":[{"field":"form.sourceMap.type","value":"1,2,3,4","isNot":true}]},{"type":"string","title":"路径","name":"path","required":true,"rules":[{"max":200,"message":"路径不得超过200个字符！"}],"props":{"placeholder":"例如: /rdos/batch"},"hidden":[{"field":"form.sourceMap.type","value":"6","isNot":true}]},{"type":"string","title":"文件类型","name":"fileType","widget":"select","required":true,"initialValue":"text","props":{"placeholder":"请选择文件类型","options":[{"label":"orc","value":"orc"},{"label":"text","value":"text"},{"label":"parquet","value":"parquet"}]},"hidden":[{"field":"form.sourceMap.type","value":"6","isNot":true}]},{"type":"string","title":"列分隔符","name":"fieldDelimiter","props":{"placeholder":"若不填写，则默认为\\\\\\\\001"},"hidden":[{"field":"form.sourceMap.type","value":"6","isNot":true},{"field":"form.sourceMap.fileType","value":"text","isNot":true}]},{"type":"string","title":"分区","name":"partition","widget":"autoComplete","props":{"placeholder":"请填写分区信息","optionsFromRequest":true,"name":"sourcemap_partition","method":"post","url":"/taier/api/dataSource/addDs/getHivePartitions","params":{"sourceId":"{{form#sourceMap.sourceId}}","tableName":"{{form#sourceMap.table}}"},"required":["sourceId","tableName"],"transformer":"table"},"depends":["sourceMap.table"],"hidden":[{"field":"form.sourceMap.type","value":"7,27,45,50","isNot":true}]},{"type":"string","title":"index","name":"index","widget":"select","required":true,"props":{"placeholder":"请选择index","optionsFromRequest":true,"name":"sourcemap_schema","method":"post","url":"/taier/api/dataSource/addDs/getAllSchemas","params":{"sourceId":"{{form#sourceMap.sourceId}}"},"required":["sourceId"],"transformer":"table"},"depends":["sourceMap.sourceId"],"hidden":[{"field":"form.sourceMap.type","value":"11,33,46","isNot":true}]},{"type":"string","title":"type","name":"indexType","widget":"select","required":true,"props":{"placeholder":"请选择indexType！","optionsFromRequest":true,"name":"sourcemap_table","method":"post","url":"/taier/api/dataSource/addDs/tablelist","params":{"sourceId":"{{form#sourceMap.sourceId}}","schema":"{{form#sourceMap.schema}}","isSys":false,"isRead":true},"required":["sourceId","schema"],"transformer":"table"},"depends":["sourceMap.index"],"hidden":[{"field":"form.sourceMap.type","value":"11,33","isNot":true}]},{"type":"string","title":"query","name":"query","widget":"textarea","rules":[{"max":1024,"message":"仅支持1-1024个任意字符"}],"props":{"placeholder":"\\\\\\"match_all\\\\\\":{}","autoSize":{"minRows":2,"maxRows":6}},"hidden":[{"field":"form.sourceMap.type","value":"11,33,46","isNot":true}]},{"type":"string","title":"高级配置","name":"extralConfig","widget":"textarea","props":{"placeholder":"以JSON格式添加高级参数，例如对关系型数据库可配置fetchSize","autoSize":{"minRows":2,"maxRows":6}},"validator":"json","hidden":[{"field":"form.sourceMap.sourceId","value":"undefined"}]},{"type":"string","title":"列","name":"column","hidden":true}]},{"type":"object","title":"选择目标","name":"targetMap","children":[{"type":"number","title":"类型","name":"type","noStyle":true,"bind":{"field":"targetMap.sourceId","transformer":"{{optionCollections.targetmap_sourceId#find.type}}"}},{"type":"number","title":"数据源","widget":"select","name":"sourceId","required":true,"props":{"placeholder":"请选择数据源","optionsFromRequest":true,"name":"targetmap_sourceId","method":"get","url":"/taier/api/dataSource/manager/queryByTenantId","transformer":"sourceId"}},{"type":"number","title":"schema","widget":"select","name":"schema","props":{"placeholder":"请选择 schema","optionsFromRequest":true,"name":"targetmap_schema","method":"post","url":"/taier/api/dataSource/addDs/getAllSchemas","params":{"sourceId":"{{form#targetMap.sourceId}}"},"required":["sourceId"],"transformer":"table"},"depends":["targetMap.sourceId"],"hidden":[{"field":"form.targetMap.type","value":"2,4","isNot":true}]},{"type":"string","title":"表名","name":"table","widget":"SelectWithCreate","required":true,"props":{"placeholder":"请选择表名","optionsFromRequest":true,"name":"targetmap_table","method":"post","url":"/taier/api/dataSource/addDs/tablelist","params":{"sourceId":"{{form#targetMap.sourceId}}","schema":"{{form#targetMap.schema}}","isSys":false,"isRead":true},"required":["sourceId"],"transformer":"table"},"depends":["targetMap.sourceId","targetMap.schema"],"hidden":[{"field":"form.targetMap.type","value":"1,2,3,4,7,8,27,45,50","isNot":true}]},{"type":"string","title":"分区","name":"partition","widget":"autoComplete","props":{"placeholder":"请填写分区信息","optionsFromRequest":true,"name":"targetmap_partition","method":"post","url":"/taier/api/dataSource/addDs/getHivePartitions","params":{"sourceId":"{{form#targetMap.sourceId}}","tableName":"{{form#targetMap.table}}"},"required":["sourceId","tableName"],"transformer":"table"},"depends":["targetMap.table"],"hidden":[{"field":"form.targetMap.type","value":"7,27,45,50","isNot":true}]},{"type":"string","title":"导入前准备语句","name":"preSql","widget":"textarea","props":{"placeholder":"请输入导入数据前执行的 SQL 脚本","autoSize":{"minRows":2,"maxRows":6}},"depends":["targetMap.type"],"hidden":[{"field":"form.targetMap.type","value":"1,2,3,4","isNot":true}]},{"type":"string","title":"导入后准备语句","name":"postSql","widget":"textarea","props":{"placeholder":"请输入导入数据后执行的 SQL 脚本","autoSize":{"minRows":2,"maxRows":6}},"depends":["targetMap.type"],"hidden":[{"field":"form.targetMap.type","value":"1,2,3,4","isNot":true}]},{"type":"string","title":"路径","name":"path","required":true,"rules":[{"max":200,"message":"路径不得超过200个字符！"}],"props":{"placeholder":"例如: /app/batch"},"hidden":[{"field":"form.targetMap.type","value":"6","isNot":true}]},{"type":"string","title":"文件名","name":"fileName","required":true,"props":{"placeholder":"请输入文件名"},"hidden":[{"field":"form.targetMap.type","value":"6","isNot":true}]},{"type":"string","title":"文件类型","name":"fileType","widget":"select","required":true,"initialValue":"orc","props":{"placeholder":"请选择文件类型","options":[{"label":"orc","value":"orc"},{"label":"text","value":"text"},{"label":"parquet","value":"parquet"}]},"hidden":[{"field":"form.targetMap.type","value":"6","isNot":true}]},{"type":"string","title":"列分隔符","name":"fieldDelimiter","props":{"placeholder":"例如: 目标为 hive 则分隔符为\\\\\\\\001"},"initialValue":",","hidden":[{"field":"form.targetMap.type","value":"6","isNot":true}]},{"type":"string","title":"编码","name":"encoding","widget":"select","required":true,"initialValue":"utf-8","props":{"placeholder":"请选择编码","options":[{"label":"utf-8","value":"utf-8"},{"label":"gdb","value":"gdb"}]},"hidden":[{"field":"form.targetMap.type","value":"6,8","isNot":true}]},{"type":"string","title":"写入模式","name":"writeMode","widget":"radio","props":{"options":[{"label":"覆盖（Insert Overwrite）","value":"replace"},{"label":"追加（Insert Into）","value":"insert"}]},"depends":["targetMap.sourceId"],"hidden":[{"field":"form.targetMap.type","value":"2,4,6,7,27,45,50","isNot":true}]},{"type":"string","title":"读取为空时的处理方式","name":"nullMode","widget":"radio","initialValue":"skip","props":{"options":[{"label":"SKIP","value":"skip"},{"label":"EMPTY","value":"empty"}]},"hidden":[{"field":"form.targetMap.type","value":"8","isNot":true}]},{"type":"string","title":"写入缓存大小","name":"writeBufferSize","widget":"inputNumber","props":{"placeholder":"请输入缓存大小","suffix":"KB"},"hidden":[{"field":"form.targetMap.type","value":"8","isNot":true}]},{"type":"string","title":"index","name":"index","widget":"select","required":true,"props":{"placeholder":"请选择index","optionsFromRequest":true,"name":"targetmap_schema","method":"post","url":"/taier/api/dataSource/addDs/getAllSchemas","params":{"sourceId":"{{form#targetMap.sourceId}}"},"required":["sourceId"],"transformer":"table"},"depends":["targetMap.sourceId"],"hidden":[{"field":"form.targetMap.type","value":"11,33,46","isNot":true}]},{"type":"string","title":"type","name":"indexType","widget":"select","required":true,"props":{"placeholder":"请选择indexType！","optionsFromRequest":true,"name":"targetmap_table","method":"post","url":"/taier/api/dataSource/addDs/tablelist","params":{"sourceId":"{{form#targetMap.sourceId}}","schema":"{{form#targetMap.schema}}","isSys":false,"isRead":true},"required":["sourceId","schema"],"transformer":"table"},"depends":["targetMap.index"],"hidden":[{"field":"form.targetMap.type","value":"11,33","isNot":true}]},{"type":"number","title":"bulkAction","name":"bulkAction","widget":"inputNumber","initialValue":100,"required":true,"props":{"min":1,"max":200000,"precision":0,"placeholder":"请输入 bulkAction"},"hidden":[{"field":"form.targetMap.type","value":"11,33,46","isNot":true}]},{"type":"string","title":"高级配置","name":"extralConfig","widget":"textarea","props":{"placeholder":"以JSON格式添加高级参数，例如对关系型数据库可配置fetchSize","autoSize":{"minRows":2,"maxRows":6}},"validator":"json","hidden":[{"field":"form.targetMap.sourceId","value":"undefined"}]},{"type":"string","title":"列","name":"column","hidden":true}]},{"type":"object","title":"字段映射","name":"mapping","children":[{"type":"any","widget":"KeyMap"}]},{"type":"object","title":"通道控制","name":"settingMap","children":[{"type":"string","name":"speed","title":"作业速率上限","widget":"autoComplete","initialValue":"不限制传输速率","required":true,"props":{"placeholder":"请选择作业速率上限","options":[{"value":"不限制传输速率"},{"value":"1"},{"value":"2"},{"value":"3"},{"value":"4"},{"value":"5"},{"value":"6"},{"value":"7"},{"value":"8"},{"value":"9"},{"value":"10"}],"suffix":"MB/s"}},{"type":"string","title":"作业并发数","name":"channel","required":true,"widget":"autoComplete","initialValue":"1","props":{"placeholder":"请选择作业并发数","options":[{"value":"1"},{"value":"2"},{"value":"3"},{"value":"4"},{"value":"5"}]}},{"type":"boolean","title":"断点续传","name":"isRestore","hidden":[{"field":"form.sourceMap.type","value":"1,2,3,4,8,19,22,24,25,28,29,31,32,35,36,40,53,54,61,71,73","isNot":true},{"field":"form.targetMap.type","value":"1,2,3,4,7,8,10,19,22,24,25,27,28,29,31,32,35,36,40,53,54,61,71,73","isNot":true}]},{"type":"string","title":"标识字段","name":"restoreColumnName","widget":"select","required":true,"props":{"placeholder":"请选择标识字段","optionsFromRequest":true,"name":"settingmap_restore","method":"post","url":"/taier/api/task/getIncreColumn","params":{"sourceId":"{{form#sourceMap.sourceId}}","tableName":"{{form#sourceMap.table}}","schema":"{{form#sourceMap.schema}}"},"required":["sourceId","tableName"],"transformer":"restore"},"hidden":[{"field":"form.settingMap.isRestore","value":"false,undefined"}]}]}]}', null, 17, 1, 'STRING', '', 1, '2022-02-11 10:28:45', '2022-02-11 10:28:45', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('-1', '虚节点', '{"formField": [], "renderKind": "virtual"}', null, 30, -1, 'STRING', '', 1, '2022-02-11 10:28:45', '2022-02-11 10:28:45', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('0', 'SparkSQL', '{"actions": ["SAVE_TASK", "RUN_TASK", "STOP_TASK", "SUBMIT_TASK", "OPERATOR_TASK"], "barItem": ["task", "dependency", "task_params", "env_params"], "formField": [], "renderKind": "editor"}', null, 30, 0, 'STRING', '', 1, '2022-02-11 10:28:45', '2022-02-11 10:28:45', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('2', 'SYNC', '{"actions": ["SAVE_TASK", "RUN_TASK", "STOP_TASK", "SUBMIT_TASK", "OPERATOR_TASK"], "barItem": ["task", "dependency", "task_config", "task_params", "env_params"], "formField": ["createModel", "syncModel"], "renderKind": "dataSync", "renderCondition": {"key": "createModel", "value": 0, "renderKind": "editor"}, "actionsCondition": {"key": "createModel", "value": 0, "actions": ["CONVERT_TASK", "SAVE_TASK", "RUN_TASK", "STOP_TASK", "SUBMIT_TASK", "OPERATOR_TASK"]}}', null, 30, 2, 'STRING', '', 1, '2022-02-11 10:28:45', '2022-02-11 10:28:45', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('5', 'FlinkSQL', '{"actions": ["GRAMMAR_TASK", "SAVE_TASK", "OPERATOR_TASK"], "barItem": ["task", "env_params"], "formField": ["componentVersion"], "renderKind": "editor", "actionsCondition": {"key": "createModel", "value": 0, "actions": ["CONVERT_TASK", "FORMAT_TASK", "GRAMMAR_TASK", "SAVE_TASK", "OPERATOR_TASK"]}, "barItemCondition": {"key": "createModel", "value": 0, "barItem": ["task", "flinksql_source", "flinksql_result", "flinksql_dimension", "env_params"]}}', null, 30, 5, 'STRING', '', 0, '2022-09-03 07:25:04', '2022-09-03 07:25:04', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('6', '实时采集', '{"actions": ["SAVE_TASK", "OPERATOR_TASK"], "barItem": ["task", "task_config", "env_params"], "formField": ["createModel", "componentVersion"], "renderKind": "streamCollection", "renderCondition": {"key": "createModel", "value": 0}, "actionsCondition": {"key": "createModel", "value": 0, "actions": ["CONVERT_TASK", "SAVE_TASK", "OPERATOR_TASK"]}}', null, 30, 6, 'STRING', '', 0, '2022-09-03 07:25:04', '2022-09-03 07:25:04', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('7', 'HiveSQL', '{"actions": ["SAVE_TASK", "RUN_TASK", "STOP_TASK", "SUBMIT_TASK", "OPERATOR_TASK"], "barItem": ["task", "dependency", "task_params", "env_params"], "formField": [], "renderKind": "editor"}', null, 30, 0, 'STRING', '', 0, '2022-09-03 07:27:25', '2022-09-03 07:27:25', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('8', 'OceanBaseSQL', '{"actions": ["SAVE_TASK", "RUN_TASK", "STOP_TASK", "SUBMIT_TASK", "OPERATOR_TASK"], "barItem": ["task", "dependency", "task_params", "env_params"], "formField": [], "renderKind": "editor"}', null, 30, 0, 'STRING', '', 0, '2022-09-03 07:27:25', '2022-09-03 07:27:25', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('10', '工作流', '{"actions": ["SAVE_TASK", "SUBMIT_TASK", "OPERATOR_TASK"], "barItem": ["task", "dependency"], "formField": [], "renderKind": "workflow"}', null, 30, 0, 'STRING', '', 0, '2022-09-03 07:27:25', '2022-09-03 07:27:25', 0);
INSERT INTO dict (dict_code, dict_name, dict_value, dict_desc, type, sort, data_type, depend_name, is_default, gmt_create, gmt_modified, is_deleted) VALUES ('11', 'Flink', '{"formField": ["resourceIdList", "mainClass", "exeArgs", "componentVersion"], "renderKind": "flink"}', null, 30, 0, 'STRING', '', 0, '2022-09-03 07:27:25', '2022-09-03 07:27:25', 0);
