local jewelrytype = {}
jewelrytype.all = {}
jewelrytype.JADEPENDANT = nil
jewelrytype.BRACELET = nil
jewelrytype.MAGICSEAL = nil
jewelrytype.BOTTLE = nil

function jewelrytype._create(os)
    local o = {}
    o.typeID = os:ReadInt32() -- 饰品类型
    o.typeName = os:ReadString() -- 程序用名字
    return o
end

function jewelrytype.get(typeID)
    return jewelrytype.all[typeID]
end

function jewelrytype._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelrytype._create(os)
        if #(v.typeName) > 0 then
            jewelrytype[v.typeName] = v
        end
        jewelrytype.all[v.typeID] = v
    end
    if jewelrytype.JADEPENDANT == nil then
        errors.enumNil("equip.jewelrytype", "JADEPENDANT");
    end
    if jewelrytype.BRACELET == nil then
        errors.enumNil("equip.jewelrytype", "BRACELET");
    end
    if jewelrytype.MAGICSEAL == nil then
        errors.enumNil("equip.jewelrytype", "MAGICSEAL");
    end
    if jewelrytype.BOTTLE == nil then
        errors.enumNil("equip.jewelrytype", "BOTTLE");
    end
end

return jewelrytype
