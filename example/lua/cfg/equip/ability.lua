local cfg = require "cfg._cfgs"

---@class cfg.equip.ability
---@field id number , 属性类型
---@field name string , 程序用名字
---@field get fun(id:number):cfg.equip.ability
---@field attack cfg.equip.ability
---@field defence cfg.equip.ability
---@field hp cfg.equip.ability
---@field critical cfg.equip.ability
---@field critical_resist cfg.equip.ability
---@field block cfg.equip.ability
---@field break_armor cfg.equip.ability
---@field all table<any,cfg.equip.ability>

local this = cfg.equip.ability

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, 2, nil, 
    'id', -- int, 属性类型
    'name' -- string, 程序用名字
    )

mk(1, "attack")
mk(2, "defence")
mk(3, "hp")
mk(4, "critical")
mk(5, "critical_resist")
mk(6, "block")
mk(7, "break_armor")

return this
