local cfg = require "cfg._cfgs"

local this = cfg.equip.rank

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, 2, nil, 
    'rankID', -- int, 稀有度
    'rankName', -- string, 程序用名字
    'rankShowName' -- string, 显示名称
    )

mk(1, "white", "下品")
mk(2, "green", "中品")
mk(3, "blue", "上品")
mk(4, "purple", "绝品")
mk(5, "yellow", "准神")

return this
