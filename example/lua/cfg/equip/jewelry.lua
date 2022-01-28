local cfg = require "cfg._cfgs"
local Beans = cfg._beans

---@class cfg.equip.jewelry
---@field iD number , 首饰ID
---@field name string , 首饰名称
---@field iconFile string , 图标ID
---@field lvlRank Beans.levelrank , 首饰等级
---@field type string , 首饰类型
---@field suitID number , 套装ID（为0是没有不属于套装，首饰品级为4的首饰该参数为套装id，其余情况为0,引用JewelrySuit.csv）
---@field keyAbility number , 关键属性类型
---@field keyAbilityValue number , 关键属性数值
---@field salePrice number , 售卖价格
---@field description string , 描述,根据Lvl和Rank来随机3个属性，第一个属性由Lvl,Rank行随机，剩下2个由Lvl和小于Rank的行里随机。Rank最小的时候都从Lvl，Rank里随机。
---@field get fun(ID:number):cfg.equip.jewelry
---@field all table<any,cfg.equip.jewelry>
---@field RefLvlRank cfg.equip.jewelryrandom
---@field RefType cfg.equip.jewelrytype
---@field NullableRefSuitID cfg.equip.jewelrysuit
---@field RefKeyAbility cfg.equip.ability

local this = cfg.equip.jewelry

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, { 
    { 'RefLvlRank', 0, cfg.equip.jewelryrandom, 'get', 4 }, 
    { 'RefType', 0, cfg.equip.jewelrytype, 'get', 5 }, 
    { 'NullableRefSuitID', 0, cfg.equip.jewelrysuit, 'get', 6 }, 
    { 'RefKeyAbility', 0, cfg.equip.ability, 'get', 7 }, }, 
    'iD', -- int, 首饰ID
    'name', -- string, 首饰名称
    'iconFile', -- string, 图标ID
    'lvlRank', -- LevelRank, 首饰等级
    'type', -- string, 首饰类型
    'suitID', -- int, 套装ID（为0是没有不属于套装，首饰品级为4的首饰该参数为套装id，其余情况为0,引用JewelrySuit.csv）
    'keyAbility', -- int, 关键属性类型
    'keyAbilityValue', -- int, 关键属性数值
    'salePrice', -- int, 售卖价格
    'description' -- string, 描述,根据Lvl和Rank来随机3个属性，第一个属性由Lvl,Rank行随机，剩下2个由Lvl和小于Rank的行里随机。Rank最小的时候都从Lvl，Rank里随机。
    )

local levelrank = Beans.levelrank

mk(1, "香木珮", "texture/t_jew_1_010.bundle", levelrank(5, 1), "Jade", 0, 2, 25, 1000, "")
mk(2, "人雄·香木珮", "texture/t_jew_1_010.bundle", levelrank(5, 2), "Jade", 0, 2, 30, 1000, "")
mk(3, "地魁·香木珮", "texture/t_jew_1_010.bundle", levelrank(5, 3), "Jade", 0, 2, 40, 1000, "")
mk(4, "天堑·香木珮", "texture/t_jew_1_014.bundle", levelrank(5, 4), "Jade", 0, 2, 55, 1000, "")
mk(5, "神王·香木珮", "texture/t_jew_1_015.bundle", levelrank(5, 5), "Jade", 0, 2, 70, 1000, "")
mk(6, "霜脂珮", "texture/t_jew_1_020.bundle", levelrank(20, 1), "Jade", 0, 2, 50, 1000, "")
mk(7, "人雄·霜脂珮", "texture/t_jew_1_020.bundle", levelrank(20, 2), "Jade", 0, 2, 60, 1000, "")
mk(8, "地魁·霜脂珮", "texture/t_jew_1_020.bundle", levelrank(20, 3), "Jade", 0, 2, 80, 1000, "")
mk(9, "天堑·霜脂珮", "texture/t_jew_1_024.bundle", levelrank(20, 4), "Jade", 0, 2, 95, 1000, "")
mk(10, "神王·霜脂珮", "texture/t_jew_1_025.bundle", levelrank(20, 5), "Jade", 1, 2, 116, 1000, "")
mk(11, "镜天珮", "texture/t_jew_1_040.bundle", levelrank(40, 1), "Jade", 0, 2, 110, 1000, "")
mk(12, "人雄·镜天珮", "texture/t_jew_1_040.bundle", levelrank(40, 2), "Jade", 0, 2, 125, 1000, "")
mk(13, "地魁·镜天珮", "texture/t_jew_1_040.bundle", levelrank(40, 3), "Jade", 0, 2, 145, 1000, "")
mk(14, "天堑·镜天珮", "texture/t_jew_1_044.bundle", levelrank(40, 4), "Jade", 2, 2, 175, 1000, "")
mk(15, "神王·镜天珮", "texture/t_jew_1_045.bundle", levelrank(40, 5), "Jade", 3, 2, 208, 1000, "")
mk(16, "凌霄珮", "texture/t_jew_1_060.bundle", levelrank(60, 1), "Jade", 0, 2, 200, 1000, "")
mk(17, "人雄·凌霄珮", "texture/t_jew_1_060.bundle", levelrank(60, 2), "Jade", 0, 2, 210, 1000, "")
mk(18, "地魁·凌霄珮", "texture/t_jew_1_060.bundle", levelrank(60, 3), "Jade", 0, 2, 235, 1000, "")
mk(19, "天堑·凌霄珮", "texture/t_jew_1_064.bundle", levelrank(60, 4), "Jade", 4, 2, 265, 1000, "")
mk(20, "神王·凌霄珮", "texture/t_jew_1_065.bundle", levelrank(60, 5), "Jade", 5, 2, 301, 1000, "")
mk(21, "虹涛珮", "texture/t_jew_1_080.bundle", levelrank(80, 1), "Jade", 0, 2, 300, 1000, "")
mk(22, "人雄·虹涛珮", "texture/t_jew_1_080.bundle", levelrank(80, 2), "Jade", 0, 2, 310, 1000, "")
mk(23, "地魁·虹涛珮", "texture/t_jew_1_080.bundle", levelrank(80, 3), "Jade", 0, 2, 330, 1000, "")
mk(24, "天堑·虹涛珮", "texture/t_jew_1_084.bundle", levelrank(80, 4), "Jade", 6, 2, 360, 1000, "")
mk(25, "神王·虹涛珮", "texture/t_jew_1_085.bundle", levelrank(80, 5), "Jade", 7, 2, 393, 1000, "")
mk(26, "梨木镯", "texture/t_jew_2_010.bundle", levelrank(5, 1), "Bracelet", 0, 1, 70, 1000, "种在地里多少年也不会长出梨的")
mk(27, "人雄·梨木镯", "texture/t_jew_2_010.bundle", levelrank(5, 2), "Bracelet", 0, 1, 100, 1000, "种在地里多少年也不会长出梨的")
mk(28, "地魁·梨木镯", "texture/t_jew_2_010.bundle", levelrank(5, 3), "Bracelet", 0, 1, 130, 1000, "种在地里多少年也不会长出梨的")
mk(29, "天堑·梨木镯", "texture/t_jew_2_014.bundle", levelrank(5, 4), "Bracelet", 0, 1, 235, 1000, "种在地里多少年也不会长出梨的")
mk(30, "神王·梨木镯", "texture/t_jew_2_015.bundle", levelrank(5, 5), "Bracelet", 0, 1, 285, 1000, "种在地里多少年也不会长出梨的")
mk(31, "镜玉镯  ", "texture/t_jew_2_020.bundle", levelrank(20, 1), "Bracelet", 0, 1, 300, 1000, "形似凝脂，吹弹可破——易碎")
mk(32, "人雄·镜玉镯", "texture/t_jew_2_020.bundle", levelrank(20, 2), "Bracelet", 0, 1, 320, 1000, "形似凝脂，吹弹可破——易碎")
mk(33, "地魁·镜玉镯", "texture/t_jew_2_020.bundle", levelrank(20, 3), "Bracelet", 0, 1, 350, 1000, "形似凝脂，吹弹可破——易碎")
mk(34, "天堑·镜玉镯", "texture/t_jew_2_024.bundle", levelrank(20, 4), "Bracelet", 0, 1, 380, 1000, "形似凝脂，吹弹可破——易碎")
mk(35, "神王·镜玉镯", "texture/t_jew_2_025.bundle", levelrank(20, 5), "Bracelet", 1, 1, 439, 1000, "形似凝脂，吹弹可破——易碎")
mk(36, "翠微镯", "texture/t_jew_2_040.bundle", levelrank(40, 1), "Bracelet", 0, 1, 450, 1000, "千万不要放在头上，切记切记")
mk(37, "人雄·翠微镯", "texture/t_jew_2_040.bundle", levelrank(40, 2), "Bracelet", 0, 1, 490, 1000, "千万不要放在头上，切记切记")
mk(38, "地魁·翠微镯", "texture/t_jew_2_040.bundle", levelrank(40, 3), "Bracelet", 0, 1, 540, 1000, "千万不要放在头上，切记切记")
mk(39, "天堑·翠微镯", "texture/t_jew_2_044.bundle", levelrank(40, 4), "Bracelet", 2, 1, 610, 1000, "千万不要放在头上，切记切记")
mk(40, "神王·翠微镯", "texture/t_jew_2_045.bundle", levelrank(40, 5), "Bracelet", 3, 1, 747, 1000, "千万不要放在头上，切记切记")

require "cfg.equip.jewelry_1"(mk)
require "cfg.equip.jewelry_2"(mk)

return this
