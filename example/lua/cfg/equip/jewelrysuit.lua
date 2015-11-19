local jewelrysuit = {}
jewelrysuit.all = {}

function jewelrysuit:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.suitID = os:ReadInt32() -- 饰品套装ID
    o.name = os:ReadText() -- 策划用名字
    o.ability1 = os:ReadInt32() -- 套装属性类型1（装备套装中的两件时增加的属性）
    o.ability1Value = os:ReadInt32() -- 套装属性1
    o.ability2 = os:ReadInt32() -- 套装属性类型2（装备套装中的三件时增加的属性）
    o.ability2Value = os:ReadInt32() -- 套装属性2
    o.ability3 = os:ReadInt32() -- 套装属性类型3（装备套装中的四件时增加的属性）
    o.ability3Value = os:ReadInt32() -- 套装属性3
    o.suitList = {} -- 部件1
    for _ = 1, os:ReadSize() do
        table.insert(o.suitList, os:ReadInt32())
    end
    return o
end

function jewelrysuit:_assign(other)
    self.suitID = other.suitID
    self.name = other.name
    self.ability1 = other.ability1
    self.ability1Value = other.ability1Value
    self.ability2 = other.ability2
    self.ability2Value = other.ability2Value
    self.ability3 = other.ability3
    self.ability3Value = other.ability3Value
    for k, v in pairs(other.suitList) do
        self.suitList[k] = v
    end
end

function jewelrysuit.get(suitID)
    return jewelrysuit.all[suitID]
end

function jewelrysuit._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelrysuit:_create(os)
        jewelrysuit.all[v.suitID] = v
    end
end

function jewelrysuit._reload(os, errors)
    local old = jewelrysuit.all
    jewelrysuit.all = {}
    jewelrysuit._initialize(os, errors)
    for k, v in pairs(jewelrysuit.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return jewelrysuit
