local cfg = require "cfg._cfgs"

local this = cfg.task.completeconditiontype

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, 2, nil, 
    "id", -- int, 任务完成条件类型（id的范围为1-100）
    "name"  -- string, 程序用名字
    )

mk(1, "KillMonster")
mk(2, "TalkNpc")
mk(3, "CollectItem")

return this
