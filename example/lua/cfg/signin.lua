local signin = {}
signin.all = {}

function signin._create(os)
    local o = {}
    o.id = os:ReadInt32() -- 礼包ID
    o.item2countMap = {} -- 普通奖励,
    for _ = 1, os:ReadSize() do
        o.item2countMap[os:ReadInt32()] = os:ReadInt32()
    end
    o.vipitem2vipcountMap = {} -- vip奖励,
    for _ = 1, os:ReadSize() do
        o.vipitem2vipcountMap[os:ReadInt32()] = os:ReadInt32()
    end
    o.viplevel = os:ReadInt32() -- 领取vip奖励的最低等级
    o.iconFile = os:ReadString() -- 礼包图标
    return o
end

function signin.get(id)
    return signin.all[id]
end

function signin._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = signin._create(os)
        signin.all[v.id] = v
    end
end

return signin
