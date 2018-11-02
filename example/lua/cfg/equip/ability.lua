local cfg = require "cfg._cfgs"

local this = cfg.equip.ability

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, 2, nil, 
    "id", -- int, 属性类型
    "name"  -- string, 程序用名字
    )

mk(1, "attack")
mk(2, "defence")
mk(3, "hp")
mk(4, "critical")
mk(5, "critical_resist")
mk(6, "block")
mk(7, "break_armor")

return this
