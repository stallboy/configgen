local cfg = require "cfg._cfgs"
local Beans = cfg._beans

local this = cfg.equip.jewelryrandom

local mk = cfg._mk.table(this, { { "all", "get", 1 }, }, nil, nil, 
    "lvlRank", -- LevelRank, 等级
    "attackRange", -- Range, 最小攻击力
    "otherRange"  -- list,Range,4, 最小防御力
    )

mk(Beans.levelrank(5, 1), Beans.range(5, 8), {Beans.range(1, 2), Beans.range(300, 315), Beans.range(15, 18), Beans.range(1, 3)})
mk(Beans.levelrank(5, 2), Beans.range(9, 13), {Beans.range(3, 5), Beans.range(320, 335), Beans.range(20, 23), Beans.range(3, 5)})
mk(Beans.levelrank(5, 3), Beans.range(15, 20), {Beans.range(6, 9), Beans.range(340, 365), Beans.range(25, 28), Beans.range(6, 8)})
mk(Beans.levelrank(5, 4), Beans.range(22, 33), {Beans.range(10, 13), Beans.range(370, 395), Beans.range(30, 33), Beans.range(9, 11)})
mk(Beans.levelrank(5, 5), Beans.range(41, 51), {Beans.range(14, 19), Beans.range(430, 480), Beans.range(40, 50), Beans.range(12, 16)})
mk(Beans.levelrank(20, 1), Beans.range(40, 50), {Beans.range(15, 20), Beans.range(490, 510), Beans.range(45, 50), Beans.range(13, 16)})
mk(Beans.levelrank(20, 2), Beans.range(51, 53), {Beans.range(21, 23), Beans.range(515, 530), Beans.range(51, 55), Beans.range(18, 20)})
mk(Beans.levelrank(20, 3), Beans.range(53, 57), {Beans.range(24, 28), Beans.range(535, 550), Beans.range(56, 60), Beans.range(22, 24)})
mk(Beans.levelrank(20, 4), Beans.range(58, 62), {Beans.range(29, 33), Beans.range(560, 590), Beans.range(63, 68), Beans.range(26, 28)})
mk(Beans.levelrank(20, 5), Beans.range(63, 79), {Beans.range(34, 40), Beans.range(640, 690), Beans.range(79, 105), Beans.range(33, 35)})
mk(Beans.levelrank(40, 1), Beans.range(60, 75), {Beans.range(35, 42), Beans.range(695, 710), Beans.range(100, 105), Beans.range(33, 35)})
mk(Beans.levelrank(40, 2), Beans.range(80, 85), {Beans.range(43, 48), Beans.range(715, 750), Beans.range(110, 115), Beans.range(37, 39)})
mk(Beans.levelrank(40, 3), Beans.range(86, 93), {Beans.range(49, 54), Beans.range(760, 810), Beans.range(119, 123), Beans.range(41, 44)})
mk(Beans.levelrank(40, 4), Beans.range(95, 103), {Beans.range(55, 60), Beans.range(830, 880), Beans.range(127, 130), Beans.range(47, 50)})
mk(Beans.levelrank(40, 5), Beans.range(107, 130), {Beans.range(62, 70), Beans.range(1000, 1200), Beans.range(140, 150), Beans.range(55, 57)})
mk(Beans.levelrank(60, 1), Beans.range(110, 130), {Beans.range(65, 70), Beans.range(1210, 1250), Beans.range(145, 150), Beans.range(55, 58)})
mk(Beans.levelrank(60, 2), Beans.range(131, 135), {Beans.range(71, 75), Beans.range(1300, 1350), Beans.range(160, 165), Beans.range(60, 63)})
mk(Beans.levelrank(60, 3), Beans.range(136, 140), {Beans.range(76, 80), Beans.range(1370, 1400), Beans.range(175, 180), Beans.range(65, 67)})
mk(Beans.levelrank(60, 4), Beans.range(141, 150), {Beans.range(81, 88), Beans.range(1430, 1480), Beans.range(185, 190), Beans.range(70, 75)})
mk(Beans.levelrank(60, 5), Beans.range(151, 189), {Beans.range(93, 105), Beans.range(1520, 1650), Beans.range(210, 220), Beans.range(80, 88)})
mk(Beans.levelrank(80, 1), Beans.range(170, 188), {Beans.range(100, 105), Beans.range(1700, 1750), Beans.range(215, 220), Beans.range(85, 88)})
mk(Beans.levelrank(80, 2), Beans.range(189, 200), {Beans.range(106, 110), Beans.range(1760, 1790), Beans.range(230, 235), Beans.range(90, 93)})
mk(Beans.levelrank(80, 3), Beans.range(201, 220), {Beans.range(113, 119), Beans.range(1820, 1850), Beans.range(240, 245), Beans.range(97, 100)})
mk(Beans.levelrank(80, 4), Beans.range(221, 224), {Beans.range(121, 127), Beans.range(1870, 1910), Beans.range(250, 255), Beans.range(105, 109)})
mk(Beans.levelrank(80, 5), Beans.range(225, 244), {Beans.range(130, 141), Beans.range(2100, 2300), Beans.range(270, 285), Beans.range(115, 120)})

return this
