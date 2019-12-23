local cfg = require "cfg._cfgs"
local Beans = cfg._beans

local this = cfg.equip.jewelry

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, { 
    { 'RefLvlRank', false, cfg.equip.jewelryrandom, 'get', 4 }, 
    { 'RefType', false, cfg.equip.jewelrytype, 'get', 5 }, 
    { 'NullableRefSuitID', false, cfg.equip.jewelrysuit, 'get', 6 }, 
    { 'RefKeyAbility', false, cfg.equip.ability, 'get', 7 }, }, 
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
mk(41, "岚海镯", "texture/t_jew_2_060.bundle", levelrank(60, 1), "Bracelet", 0, 1, 760, 1000, "似有龙灵附于其上")
mk(42, "人雄·岚海镯", "texture/t_jew_2_060.bundle", levelrank(60, 2), "Bracelet", 0, 1, 800, 1000, "似有龙灵附于其上")
mk(43, "地魁·岚海镯", "texture/t_jew_2_060.bundle", levelrank(60, 3), "Bracelet", 0, 1, 860, 1000, "似有龙灵附于其上")
mk(44, "天堑·岚海镯", "texture/t_jew_2_064.bundle", levelrank(60, 4), "Bracelet", 4, 1, 930, 1000, "似有龙灵附于其上")
mk(45, "神王·岚海镯", "texture/t_jew_2_065.bundle", levelrank(60, 5), "Bracelet", 5, 1, 1055, 1000, "似有龙灵附于其上")
mk(46, "残凤镯", "texture/t_jew_2_080.bundle", levelrank(80, 1), "Bracelet", 0, 1, 1050, 1000, "似有凤魂凝于其中")
mk(47, "人雄·残凤镯", "texture/t_jew_2_080.bundle", levelrank(80, 2), "Bracelet", 0, 1, 1100, 1000, "似有凤魂凝于其中")
mk(48, "地魁·残凤镯", "texture/t_jew_2_080.bundle", levelrank(80, 3), "Bracelet", 0, 1, 1160, 1000, "似有凤魂凝于其中")
mk(49, "天堑·残凤镯", "texture/t_jew_2_084.bundle", levelrank(80, 4), "Bracelet", 6, 1, 1260, 1000, "似有凤魂凝于其中")
mk(50, "神王·残凤镯", "texture/t_jew_2_085.bundle", levelrank(80, 5), "Bracelet", 7, 1, 1363, 1000, "似有凤魂凝于其中")
mk(51, "枯木印", "texture/t_jew_3_010.bundle", levelrank(5, 1), "Magic", 0, 3, 500, 1000, "剩余使用次数：3")
mk(52, "人雄·枯木印", "texture/t_jew_3_010.bundle", levelrank(5, 2), "Magic", 0, 3, 800, 1000, "剩余使用次数：3")
mk(53, "地魁·枯木印", "texture/t_jew_3_010.bundle", levelrank(5, 3), "Magic", 0, 3, 1500, 1000, "剩余使用次数：3")
mk(54, "天堑·枯木印", "texture/t_jew_3_014.bundle", levelrank(5, 4), "Magic", 0, 3, 2100, 1000, "剩余使用次数：3")
mk(55, "神王·枯木印", "texture/t_jew_3_015.bundle", levelrank(5, 5), "Magic", 0, 3, 2926, 1000, "剩余使用次数：3")
mk(56, "巳结印", "texture/t_jew_3_020.bundle", levelrank(20, 1), "Magic", 0, 3, 2800, 1000, "印柄不论夏秋皆会有寒意袭来")
mk(57, "人雄·巳结印", "texture/t_jew_3_020.bundle", levelrank(20, 2), "Magic", 0, 3, 3100, 1000, "印柄不论夏秋皆会有寒意袭来")
mk(58, "地魁·巳结印", "texture/t_jew_3_020.bundle", levelrank(20, 3), "Magic", 0, 3, 3500, 1000, "印柄不论夏秋皆会有寒意袭来")
mk(59, "天堑·巳结印", "texture/t_jew_3_024.bundle", levelrank(20, 4), "Magic", 0, 3, 3850, 1000, "印柄不论夏秋皆会有寒意袭来")
mk(60, "神王·巳结印", "texture/t_jew_3_025.bundle", levelrank(20, 5), "Magic", 1, 3, 4466, 1000, "印柄不论夏秋皆会有寒意袭来")
mk(61, "寅玄印", "texture/t_jew_3_040.bundle", levelrank(40, 1), "Magic", 0, 3, 4400, 1000, "夜深人静的时候会从其中发出野兽的低吼")
mk(62, "人雄·寅玄印", "texture/t_jew_3_040.bundle", levelrank(40, 2), "Magic", 0, 3, 5000, 1000, "夜深人静的时候会从其中发出野兽的低吼")
mk(63, "地魁·寅玄印", "texture/t_jew_3_040.bundle", levelrank(40, 3), "Magic", 0, 3, 5700, 1000, "夜深人静的时候会从其中发出野兽的低吼")
mk(64, "天堑·寅玄印", "texture/t_jew_3_044.bundle", levelrank(40, 4), "Magic", 2, 3, 6500, 1000, "夜深人静的时候会从其中发出野兽的低吼")
mk(65, "神王·寅玄印", "texture/t_jew_3_045.bundle", levelrank(40, 5), "Magic", 3, 3, 7546, 1000, "夜深人静的时候会从其中发出野兽的低吼")
mk(66, "麒珞印", "texture/t_jew_3_060.bundle", levelrank(60, 1), "Magic", 0, 3, 7500, 1000, "雄者为麒，可召雷电")
mk(67, "人雄·麒珞印", "texture/t_jew_3_060.bundle", levelrank(60, 2), "Magic", 0, 3, 8100, 1000, "雄者为麒，可召雷电")
mk(68, "地魁·麒珞印", "texture/t_jew_3_060.bundle", levelrank(60, 3), "Magic", 0, 3, 8800, 1000, "雄者为麒，可召雷电")
mk(69, "天堑·麒珞印", "texture/t_jew_3_064.bundle", levelrank(60, 4), "Magic", 4, 3, 9700, 1000, "雄者为麒，可召雷电")
mk(70, "神王·麒珞印", "texture/t_jew_3_065.bundle", levelrank(60, 5), "Magic", 5, 3, 10626, 1000, "雄者为麒，可召雷电")
mk(71, "麟荒印", "texture/t_jew_3_080.bundle", levelrank(80, 1), "Magic", 0, 3, 10000, 1000, "雌者为麟，可治愈疾患")
mk(72, "人雄·麟荒印", "texture/t_jew_3_080.bundle", levelrank(80, 2), "Magic", 0, 3, 10700, 1000, "雌者为麟，可治愈疾患")
mk(73, "地魁·麟荒印", "texture/t_jew_3_080.bundle", levelrank(80, 3), "Magic", 0, 3, 11500, 1000, "雌者为麟，可治愈疾患")
mk(74, "天堑·麟荒印", "texture/t_jew_3_084.bundle", levelrank(80, 4), "Magic", 6, 3, 12600, 1000, "雌者为麟，可治愈疾患")
mk(75, "神王·麟荒印", "texture/t_jew_3_085.bundle", levelrank(80, 5), "Magic", 7, 3, 13706, 1000, "雌者为麟，可治愈疾患")
mk(76, "榆木铃", "texture/t_jew_4_010.bundle", levelrank(5, 1), "Bottle", 0, 4, 55, 1000, "榆木一块，再怎么摇也没救了")
mk(77, "人雄·榆木铃", "texture/t_jew_4_010.bundle", levelrank(5, 2), "Bottle", 0, 4, 110, 1000, "榆木一块，再怎么摇也没救了")
mk(78, "地魁·榆木铃", "texture/t_jew_4_010.bundle", levelrank(5, 3), "Bottle", 0, 4, 130, 1000, "榆木一块，再怎么摇也没救了")
mk(79, "天堑·榆木铃", "texture/t_jew_4_014.bundle", levelrank(5, 4), "Bottle", 0, 4, 150, 1000, "榆木一块，再怎么摇也没救了")
mk(80, "神王·榆木铃", "texture/t_jew_4_015.bundle", levelrank(5, 5), "Bottle", 0, 4, 171, 1000, "榆木一块，再怎么摇也没救了")
mk(81, "残雪铃", "texture/t_jew_4_020.bundle", levelrank(20, 1), "Bottle", 0, 4, 165, 1000, "轻轻摇动便有雪花落下，可是并没有爬烟囱的老人")
mk(82, "人雄·残雪铃", "texture/t_jew_4_020.bundle", levelrank(20, 2), "Bottle", 0, 4, 175, 1000, "轻轻摇动便有雪花落下，可是并没有爬烟囱的老人")
mk(83, "地魁·残雪铃", "texture/t_jew_4_020.bundle", levelrank(20, 3), "Bottle", 0, 4, 200, 1000, "轻轻摇动便有雪花落下，可是并没有爬烟囱的老人")
mk(84, "天堑·残雪铃", "texture/t_jew_4_024.bundle", levelrank(20, 4), "Bottle", 0, 4, 230, 1000, "轻轻摇动便有雪花落下，可是并没有爬烟囱的老人")
mk(85, "神王·残雪铃", "texture/t_jew_4_025.bundle", levelrank(20, 5), "Bottle", 1, 4, 264, 1000, "轻轻摇动便有雪花落下，可是并没有爬烟囱的老人")
mk(86, "琉璃铃", "texture/t_jew_4_040.bundle", levelrank(40, 1), "Bottle", 0, 4, 260, 1000, "清音清脆，连蝶鸟听罢都会驻足欣赏")
mk(87, "人雄·琉璃铃", "texture/t_jew_4_040.bundle", levelrank(40, 2), "Bottle", 0, 4, 280, 1000, "清音清脆，连蝶鸟听罢都会驻足欣赏")
mk(88, "地魁·琉璃铃", "texture/t_jew_4_040.bundle", levelrank(40, 3), "Bottle", 0, 4, 320, 1000, "清音清脆，连蝶鸟听罢都会驻足欣赏")
mk(89, "天堑·琉璃铃", "texture/t_jew_4_044.bundle", levelrank(40, 4), "Bottle", 2, 4, 380, 1000, "清音清脆，连蝶鸟听罢都会驻足欣赏")
mk(90, "神王·琉璃铃", "texture/t_jew_4_045.bundle", levelrank(40, 5), "Bottle", 3, 4, 449, 1000, "清音清脆，连蝶鸟听罢都会驻足欣赏")
mk(91, "兹崖铃", "texture/t_jew_4_060.bundle", levelrank(60, 1), "Bottle", 0, 4, 440, 1000, "摇动产生的魔音有乱人心智之怪力")
mk(92, "人雄·兹崖铃", "texture/t_jew_4_060.bundle", levelrank(60, 2), "Bottle", 0, 4, 460, 1000, "摇动产生的魔音有乱人心智之怪力")
mk(93, "地魁·兹崖铃", "texture/t_jew_4_060.bundle", levelrank(60, 3), "Bottle", 0, 4, 500, 1000, "摇动产生的魔音有乱人心智之怪力")
mk(94, "天堑·兹崖铃", "texture/t_jew_4_064.bundle", levelrank(60, 4), "Bottle", 4, 4, 560, 1000, "摇动产生的魔音有乱人心智之怪力")
mk(95, "神王·兹崖铃", "texture/t_jew_4_065.bundle", levelrank(60, 5), "Bottle", 5, 4, 633, 1000, "摇动产生的魔音有乱人心智之怪力")
mk(96, "沁晶铃", "texture/t_jew_4_080.bundle", levelrank(80, 1), "Bottle", 0, 4, 620, 1000, "镶嵌其上的宝石具有扩音作用，能使声音传的很远")
mk(97, "人雄·沁晶铃", "texture/t_jew_4_080.bundle", levelrank(80, 2), "Bottle", 0, 4, 655, 1000, "镶嵌其上的宝石具有扩音作用，能使声音传的很远")
mk(98, "地魁·沁晶铃", "texture/t_jew_4_080.bundle", levelrank(80, 3), "Bottle", 0, 4, 700, 1000, "镶嵌其上的宝石具有扩音作用，能使声音传的很远")
mk(99, "天堑·沁晶铃", "texture/t_jew_4_084.bundle", levelrank(80, 4), "Bottle", 6, 4, 750, 1000, "镶嵌其上的宝石具有扩音作用，能使声音传的很远")
mk(100, "神王·沁晶铃", "texture/t_jew_4_085.bundle", levelrank(80, 5), "Bottle", 7, 4, 818, 1000, "镶嵌其上的宝石具有扩音作用，能使声音传的很远")

return this
