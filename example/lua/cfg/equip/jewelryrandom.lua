local cfg = require "cfg._cfgs"
local Beans = cfg._beans

---@class cfg.equip.jewelryrandom
---@field lvlRank Beans.levelrank , 等级
---@field attackRange Beans.range , 最小攻击力
---@field otherRange table<number,Beans.range> , 最小防御力
---@field testPack table<number,Beans.equip.testpackbean> , 测试pack
---@field get fun(LvlRank:Beans.levelrank):cfg.equip.jewelryrandom
---@field all table<any,cfg.equip.jewelryrandom>

local this = cfg.equip.jewelryrandom

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, nil, 
    'lvlRank', -- LevelRank, 等级
    'attackRange', -- Range, 最小攻击力
    'otherRange', -- list,Range,4, 最小防御力
    'testPack' -- list,TestPackBean, 测试pack
    )

local testpackbean = Beans.equip.testpackbean
local levelrank = Beans.levelrank
local range = Beans.range

local E = cfg._mk.E

local R = cfg._mk.R
local A = {}
A[1] = R(range(3, 5))
A[2] = R(range(40, 50))
A[3] = R(range(15, 20))
A[4] = R(range(33, 35))
A[5] = R(range(100, 105))

mk(levelrank(5, 1), range(5, 8), {range(1, 2), range(300, 315), range(15, 18), range(1, 1)}, {testpackbean("range1", range(100, 120)), testpackbean("range2", range(300, 400))})
mk(levelrank(5, 2), range(9, 13), {A[1], range(320, 335), range(20, 23), A[1]}, E)
mk(levelrank(5, 3), A[3], {range(6, 9), range(340, 365), range(25, 28), range(6, 8)}, E)
mk(levelrank(5, 4), range(22, 33), {range(10, 13), range(370, 395), range(30, 33), range(9, 11)}, E)
mk(levelrank(5, 5), range(41, 51), {range(14, 19), range(430, 480), A[2], range(12, 16)}, E)
mk(levelrank(20, 1), A[2], {A[3], range(490, 510), range(45, 50), range(13, 16)}, E)
mk(levelrank(20, 2), range(51, 53), {range(21, 23), range(515, 530), range(51, 55), range(18, 20)}, E)
mk(levelrank(20, 3), range(53, 57), {range(24, 28), range(535, 550), range(56, 60), range(22, 24)}, E)
mk(levelrank(20, 4), range(58, 62), {range(29, 33), range(560, 590), range(63, 68), range(26, 28)}, E)
mk(levelrank(20, 5), range(63, 79), {range(34, 40), range(640, 690), range(79, 105), A[4]}, E)
mk(levelrank(40, 1), range(60, 75), {range(35, 42), range(695, 710), A[5], A[4]}, E)
mk(levelrank(40, 2), range(80, 85), {range(43, 48), range(715, 750), range(110, 115), range(37, 39)}, E)
mk(levelrank(40, 3), range(86, 93), {range(49, 54), range(760, 810), range(119, 123), range(41, 44)}, E)
mk(levelrank(40, 4), range(95, 103), {range(55, 60), range(830, 880), range(127, 130), range(47, 50)}, E)
mk(levelrank(40, 5), range(107, 130), {range(62, 70), range(1000, 1200), range(140, 150), range(55, 57)}, E)
mk(levelrank(60, 1), range(110, 130), {range(65, 70), range(1210, 1250), range(145, 150), range(55, 58)}, E)
mk(levelrank(60, 2), range(131, 135), {range(71, 75), range(1300, 1350), range(160, 165), range(60, 63)}, E)
mk(levelrank(60, 3), range(136, 140), {range(76, 80), range(1370, 1400), range(175, 180), range(65, 67)}, E)
mk(levelrank(60, 4), range(141, 150), {range(81, 88), range(1430, 1480), range(185, 190), range(70, 75)}, E)
mk(levelrank(60, 5), range(151, 189), {range(93, 105), range(1520, 1650), range(210, 220), range(80, 88)}, E)
mk(levelrank(80, 1), range(170, 188), {A[5], range(1700, 1750), range(215, 220), range(85, 88)}, E)
mk(levelrank(80, 2), range(189, 200), {range(106, 110), range(1760, 1790), range(230, 235), range(90, 93)}, E)
mk(levelrank(80, 3), range(201, 220), {range(113, 119), range(1820, 1850), range(240, 245), range(97, 100)}, E)
mk(levelrank(80, 4), range(221, 224), {range(121, 127), range(1870, 1910), range(250, 255), range(105, 109)}, E)
mk(levelrank(80, 5), range(225, 244), {range(130, 141), range(2100, 2300), range(270, 285), range(115, 120)}, E)

return this
