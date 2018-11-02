local cfg = require "cfg._cfgs"

local this = cfg.signin

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, nil, nil, 
    "id", -- int, 礼包ID
    "item2countMap", -- map,int,int,5, 普通奖励
    "vipitem2vipcountMap", -- map,int,int,2, vip奖励
    "viplevel", -- int, 领取vip奖励的最低等级
    "iconFile"  -- string, 礼包图标
    )

mk(1, {[10001] = 1}, {}, 0, "texture/t_i10005.bundle")
mk(2, {[10014] = 1}, {}, 0, "texture/t_i10006.bundle")
mk(3, {[30001] = 1}, {}, 0, "texture/t_i10007.bundle")
mk(4, {[10001] = 5, [30002] = 5, [30001] = 5}, {[10001] = 10}, 0, "texture/t_i10008.bundle")
mk(5, {[10001] = 1}, {}, 0, "texture/t_i10009.bundle")
mk(6, {[10025] = 1}, {}, 0, "texture/t_i10010.bundle")
mk(7, {[30001] = 1}, {}, 0, "texture/t_i10005.bundle")
mk(8, {[10001] = 5, [30002] = 5, [30001] = 5}, {[10001] = 10}, 0, "texture/t_i10006.bundle")
mk(9, {[10001] = 1}, {}, 0, "texture/t_i10007.bundle")
mk(10, {[10040] = 1}, {}, 0, "texture/t_i10008.bundle")
mk(11, {[30001] = 1}, {}, 0, "texture/t_i10009.bundle")
mk(12, {[10001] = 5, [30002] = 5, [30001] = 5}, {[10001] = 10}, 0, "texture/t_i10010.bundle")

return this
