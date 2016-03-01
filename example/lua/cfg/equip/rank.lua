local rank = {}
rank.white = nil
rank.green = nil
rank.blue = nil
rank.purple = nil
rank.yellow = nil

function rank:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.rankID = os:ReadInt32() -- 稀有度
    o.rankName = os:ReadString() -- 程序用名字
    o.rankShowName = os:ReadString() -- 显示名称
    return o
end

function rank:_assign(other)
    self.rankID = other.rankID
    self.rankName = other.rankName
    self.rankShowName = other.rankShowName
end

rank.all = {}
function rank.get(rankID)
    return rank.all[rankID]
end

function rank._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = rank:_create(os)
        if #(v.rankName) > 0 then
            rank[v.rankName] = v
        end
        rank.all[v.rankID] = v
    end
    if rank.white == nil then
        errors.enumNil("equip.rank", "white");
    end
    if rank.green == nil then
        errors.enumNil("equip.rank", "green");
    end
    if rank.blue == nil then
        errors.enumNil("equip.rank", "blue");
    end
    if rank.purple == nil then
        errors.enumNil("equip.rank", "purple");
    end
    if rank.yellow == nil then
        errors.enumNil("equip.rank", "yellow");
    end
end

function rank._reload(os, errors)
    local old = rank.all
    rank.all = {}
    rank._initialize(os, errors)
    for k, v in pairs(rank.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return rank
