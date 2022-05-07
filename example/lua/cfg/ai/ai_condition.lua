local cfg = require "cfg._cfgs"

---@class cfg.ai.ai_condition
---@field iD number , ID
---@field desc string , 描述
---@field formulaID number , 公式
---@field argIList table<number,number> , 参数(int)1
---@field argSList table<number,number> , 参数(string)1
---@field get fun(ID:number):cfg.ai.ai_condition
---@field all table<any,cfg.ai.ai_condition>

local this = cfg.ai.ai_condition

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, nil, 
    'iD', -- int, ID
    'desc', -- string, 描述
    'formulaID', -- int, 公式
    'argIList', -- list,int,6, 参数(int)1
    'argSList' -- list,int,3, 参数(string)1
    )

local E = cfg._mk.E
local R = cfg._mk.R

local A = {}
A[1] = R({30})
A[2] = R({1800})

mk(1, "受到伤害", 1, E, E)
mk(2, "血量20%到80%", 2, R({20, 80}), E)
mk(10012, "游戏开始召唤猴子", 6, A[1], E)
mk(10013, "游戏开始召唤猩猩", 6, A[1], E)
mk(1017001, "出生后30秒", 6, R({900}), E)
mk(1017007, "出生后", 6, R({5}), E)
mk(1018001, "出生后60秒", 6, A[2], E)
mk(5004001, "出生后", 6, A[1], E)
mk(5004002, "出生后1分", 6, A[2], E)
mk(5004003, "出生后2分", 6, R({3600}), E)
mk(5004004, "出生后3分", 6, R({5400}), E)
mk(200501, "出生后一帧", 6, R({15}), E)
mk(103011, "出生后的瞬间", 6, R({1}), E)
mk(20801, "游戏开始召唤猩猩", 6, A[1], E)
mk(20017, "出生后3秒", 6, R({90}), E)
mk(20020, "血量低于40%", 3, R({40}), E)
mk(20021, "死亡", 8, E, E)
mk(200201, "出生后10秒", 6, R({300}), E)

return this
