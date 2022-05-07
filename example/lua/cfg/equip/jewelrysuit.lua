local cfg = require "cfg._cfgs"

---@class cfg.equip.jewelrysuit
---@field suitID number , 饰品套装ID
---@field ename string 
---@field name text , 策划用名字
---@field ability1 number , 套装属性类型1（装备套装中的两件时增加的属性）
---@field ability1Value number , 套装属性1
---@field ability2 number , 套装属性类型2（装备套装中的三件时增加的属性）
---@field ability2Value number , 套装属性2
---@field ability3 number , 套装属性类型3（装备套装中的四件时增加的属性）
---@field ability3Value number , 套装属性3
---@field suitList table<number,number> , 部件1
---@field get fun(SuitID:number):cfg.equip.jewelrysuit
---@field SpecialSuit cfg.equip.jewelrysuit
---@field all table<any,cfg.equip.jewelrysuit>

local this = cfg.equip.jewelrysuit

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, 2, nil, 
    'suitID', -- int, 饰品套装ID
    'ename', -- string
    'name', -- text, 策划用名字
    'ability1', -- int, 套装属性类型1（装备套装中的两件时增加的属性）
    'ability1Value', -- int, 套装属性1
    'ability2', -- int, 套装属性类型2（装备套装中的三件时增加的属性）
    'ability2Value', -- int, 套装属性2
    'ability3', -- int, 套装属性类型3（装备套装中的四件时增加的属性）
    'ability3Value', -- int, 套装属性3
    'suitList' -- list,int,4, 部件1
    )

local R = cfg._mk.R

mk(1, "", "20级橙色套装", 3, 750, 2, 40, 6, 222, R({10, 35, 60, 85}))
mk(2, "", "40级紫色套装", 2, 40, 3, 1200, 1, 123, R({14, 39, 64, 89}))
mk(3, "", "40级橙色套装", 3, 1600, 4, 135, 6, 398, R({15, 40, 65, 90}))
mk(4, "SpecialSuit", "60级紫色套装", 2, 54, 3, 1800, 1, 186, R({19, 44, 69, 94}))
mk(5, "", "60级橙色套装", 3, 2600, 4, 226, 6, 556, R({20, 45, 70, 95}))
mk(6, "", "80级紫色套装", 2, 70, 3, 2900, 1, 243, R({24, 49, 74, 99}))
mk(7, "", "80级橙色套装", 3, 3800, 4, 286, 6, 711, R({25, 50, 75, 100}))

return this
