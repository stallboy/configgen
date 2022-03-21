local cfg = require "cfg._cfgs"

---@class cfg.other.signin
---@field id number , 礼包ID
---@field item2countMap table<number,number> , 普通奖励
---@field vipitem2vipcountMap table<number,number> , vip奖励
---@field viplevel number , 领取vip奖励的最低等级
---@field iconFile string , 礼包图标
---@field get fun(id:number):cfg.other.signin
---@field getByIdViplevel fun(id:number,viplevel:number):cfg.other.signin
---@field all table<any,cfg.other.signin>

local this = cfg.other.signin

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, { 'IdViplevelMap', 'getByIdViplevel', 1, 4 }, }, nil, { 
    { 'RefVipitem2vipcountMap', 3, cfg.other.loot, 'get', 3 }, }, 
    'id', -- int, 礼包ID
    'item2countMap', -- map,int,int,5, 普通奖励
    'vipitem2vipcountMap', -- map,int,int,2, vip奖励
    'viplevel', -- int, 领取vip奖励的最低等级
    'iconFile' -- string, 礼包图标
    )

local E = cfg._mk.E

local R = cfg._mk.R
local A = {}
A[1] = R({[10001] = 1})
A[2] = R({[30001] = 1})
A[3] = R({[10001] = 5, [30002] = 5, [30001] = 5})
A[4] = R({[10001] = 10})

mk(1, A[1], E, 0, "texture/t_i10005.bundle")
mk(2, {[10014] = 1}, E, 0, "texture/t_i10006.bundle")
mk(3, A[2], E, 0, "texture/t_i10007.bundle")
mk(4, A[3], A[4], 0, "texture/t_i10008.bundle")
mk(5, A[1], E, 0, "texture/t_i10009.bundle")
mk(6, {[10025] = 1}, E, 0, "texture/t_i10010.bundle")
mk(7, A[2], E, 0, "texture/t_i10005.bundle")
mk(8, A[3], A[4], 0, "texture/t_i10006.bundle")
mk(9, A[1], E, 0, "texture/t_i10007.bundle")
mk(10, {[10040] = 1}, E, 0, "texture/t_i10008.bundle")
mk(11, A[2], E, 0, "texture/t_i10009.bundle")
mk(12, A[3], A[4], 0, "texture/t_i10010.bundle")

return this
