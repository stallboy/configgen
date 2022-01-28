local cfg = require "cfg._cfgs"

---@class cfg.equip.jewelrytype
---@field typeName string , 程序用名字
---@field get fun(TypeName:string):cfg.equip.jewelrytype
---@field Jade cfg.equip.jewelrytype
---@field Bracelet cfg.equip.jewelrytype
---@field Magic cfg.equip.jewelrytype
---@field Bottle cfg.equip.jewelrytype
---@field all table<any,cfg.equip.jewelrytype>

local this = cfg.equip.jewelrytype

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, 1, nil, 
    'typeName' -- string, 程序用名字
    )

mk("Jade")
mk("Bracelet")
mk("Magic")
mk("Bottle")

return this
