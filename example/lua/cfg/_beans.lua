local levelrank = {}
function levelrank._create(os)
    local o = {}
    o.level = os:ReadInt32() -- 等级
    o.rank = os:ReadInt32() -- 品质
    o.RefRank = nil
    return o
end

local position = {}
function position._create(os)
    local o = {}
    o.x = os:ReadInt32()
    o.y = os:ReadInt32()
    o.z = os:ReadInt32()
    return o
end

local range = {}
function range._create(os)
    local o = {}
    o.min = os:ReadInt32() -- 最小
    o.max = os:ReadInt32() -- 最大
    return o
end

return {
    levelrank = levelrank,
    position = position,
    range = range,
}
