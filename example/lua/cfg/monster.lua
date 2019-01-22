local cfg = require "cfg._cfgs"
local Beans = cfg._beans

local this = cfg.monster

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, nil, nil, 
    "id", -- int, id
    "posList"  -- list,Position
    )

local position = Beans.position

mk(1, {position(1, 2, 3), position(11, 22, 33), position(111, 222, 333)})
mk(2, {position(33, 44, 55)})

return this
