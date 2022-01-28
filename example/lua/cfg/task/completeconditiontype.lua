local cfg = require "cfg._cfgs"

---@class cfg.task.completeconditiontype
---@field id number , 任务完成条件类型（id的范围为1-100）
---@field name string , 程序用名字
---@field get fun(id:number):cfg.task.completeconditiontype
---@field KillMonster cfg.task.completeconditiontype
---@field TalkNpc cfg.task.completeconditiontype
---@field CollectItem cfg.task.completeconditiontype
---@field ConditionAnd cfg.task.completeconditiontype
---@field Chat cfg.task.completeconditiontype
---@field TestNoColumn cfg.task.completeconditiontype
---@field all table<any,cfg.task.completeconditiontype>

local this = cfg.task.completeconditiontype

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, 2, nil, 
    'id', -- int, 任务完成条件类型（id的范围为1-100）
    'name' -- string, 程序用名字
    )

mk(1, "KillMonster")
mk(2, "TalkNpc")
mk(3, "CollectItem")
mk(4, "ConditionAnd")
mk(5, "Chat")
mk(6, "TestNoColumn")

return this
