local cfg = require "cfg._cfgs"

local Beans = {}
cfg._beans = Beans

local bean = cfg._mk.bean
local action = cfg._mk.action

Beans.levelrank = bean({ 
    { 'RefRank', false, cfg.equip.rank, 'get', 2 }, }, 
    'level', -- int, 等级
    'rank' -- int, 品质
    )
Beans.position = bean(nil, 
    'x', -- int
    'y', -- int
    'z' -- int
    )
Beans.range = bean(nil, 
    'min', -- int, 最小
    'max' -- int, 最大
    )
Beans.task = {}
Beans.task.completecondition = {}
Beans.task.completecondition.killmonster = action("KillMonster", { 
    { 'RefMonsterid', false, cfg.monster, 'get', 1 }, }, 
    'monsterid', -- int
    'count' -- int
    )
Beans.task.completecondition.talknpc = action("TalkNpc", nil, 
    'npcid' -- int
    )
Beans.task.completecondition.chat = action("Chat", nil, 
    'msg' -- string
    )
Beans.task.completecondition.conditionand = action("ConditionAnd", nil, 
    'cond1', -- task.completecondition
    'cond2' -- task.completecondition
    )
Beans.task.completecondition.collectitem = action("CollectItem", nil, 
    'itemid', -- int
    'count' -- int
    )

return Beans
