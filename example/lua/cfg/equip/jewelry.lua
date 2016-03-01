local Beans = require("cfg._beans")

local jewelry = {}

function jewelry:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.iD = os:ReadInt32() -- 首饰ID
    o.name = os:ReadString() -- 首饰名称
    o.iconFile = os:ReadString() -- 图标ID
    o.lvlRank = Beans.levelrank:_create(os) -- 首饰等级
    o.type = os:ReadString() -- 首饰类型
    o.RefType = nil
    o.suitID = os:ReadInt32() -- 套装ID（为0是没有不属于套装，首饰品级为4的首饰该参数为套装id，其余情况为0,引用JewelrySuit.csv）
    o.NullableRefSuitID = nil
    o.keyAbility = os:ReadInt32() -- 关键属性类型
    o.RefKeyAbility = nil
    o.keyAbilityValue = os:ReadInt32() -- 关键属性数值
    o.salePrice = os:ReadInt32() -- 售卖价格
    o.description = os:ReadString() -- 描述,根据Lvl和Rank来随机3个属性，第一个属性由Lvl,Rank行随机，剩下2个由Lvl和小于Rank的行里随机。Rank最小的时候都从Lvl，Rank里随机。
    return o
end

function jewelry:_assign(other)
    self.iD = other.iD
    self.name = other.name
    self.iconFile = other.iconFile
    self.lvlRank:_assign(other.lvlRank)
    self.type = other.type
    self.suitID = other.suitID
    self.keyAbility = other.keyAbility
    self.keyAbilityValue = other.keyAbilityValue
    self.salePrice = other.salePrice
    self.description = other.description
end

jewelry.all = {}
function jewelry.get(iD)
    return jewelry.all[iD]
end

function jewelry._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelry:_create(os)
        jewelry.all[v.iD] = v
    end
end

function jewelry._reload(os, errors)
    local old = jewelry.all
    jewelry.all = {}
    jewelry._initialize(os, errors)
    for k, v in pairs(jewelry.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return jewelry
