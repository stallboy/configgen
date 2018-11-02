local cfg = require "cfg._cfgs"

local this = cfg.equip.jewelrytype

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, 1, nil, 
    "typeName"  -- string, 程序用名字
    )

mk("Jade")
mk("Bracelet")
mk("Magic")
mk("Bottle")

return this
