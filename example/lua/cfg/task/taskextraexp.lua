local cfg = require "cfg._cfgs"

---@class cfg.task.taskextraexp
---@field taskid number , 任务完成条件类型（id的范围为1-100）
---@field extraexp number , 额外奖励经验
---@field get fun(taskid:number):cfg.task.taskextraexp
---@field all table<any,cfg.task.taskextraexp>

local this = cfg.task.taskextraexp

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, nil, 
    'taskid', -- int, 任务完成条件类型（id的范围为1-100）
    'extraexp' -- int, 额外奖励经验
    )

mk(1, 1000)

return this
