local cfg = require "cfg._cfgs"

local this = cfg.task.taskextraexp

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, nil, 
    'taskid', -- int, 任务完成条件类型（id的范围为1-100）
    'extraexp' -- int, 额外奖励经验
    )

mk(1, 1000)

return this
