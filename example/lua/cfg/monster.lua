local cfg = require "cfg._cfgs"
local Beans = cfg._beans

local this = cfg.monster

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, nil, nil, 
    "id", -- int, id
    "posList"  -- list,Position
    )

mk(1, {Beans.position(1, 2, 3), Beans.position(11, 22, 33), Beans.position(111, 222, 333)})
mk(2, {Beans.position(33, 44, 55)})

return this
