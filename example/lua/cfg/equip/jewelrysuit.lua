local jewelrysuit = {}
jewelrysuit.all = {}

function jewelrysuit._create(os)
    local o = {}
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

function jewelrysuit.get(suitID)
    return jewelrysuit.all[suitID]
end

function jewelrysuit._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelrysuit._create(os)
        jewelrysuit.all[v.suitID] = v
    end
end

return jewelrysuit
