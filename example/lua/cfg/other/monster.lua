local cfg = require "cfg._cfgs"
local Beans = cfg._beans

---@class cfg.other.monster
---@field id number , id
---@field posList table<number,Beans.position> 
---@field get fun(id:number):cfg.other.monster
---@field all table<any,cfg.other.monster>

local this = cfg.other.monster

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, nil, 
    'id', -- int, id
    'posList' -- list,Position
    )

local position = Beans.position

local R = cfg._mk.R

mk(1, R({position(1, 2, 3), position(11, 22, 33), position(111, 222, 333)}))
mk(2, R({position(33, 44, 55)}))

return this
