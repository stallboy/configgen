local jewelrytype = {}
jewelrytype.Jade = nil
jewelrytype.Bracelet = nil
jewelrytype.Magic = nil
jewelrytype.Bottle = nil

function jewelrytype:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.typeID = os:ReadInt32() -- 饰品类型
    o.typeName = os:ReadString() -- 程序用名字
    return o
end

function jewelrytype:_assign(other)
    self.typeID = other.typeID
    self.typeName = other.typeName
end

jewelrytype.all = {}
function jewelrytype.get(typeID)
    return jewelrytype.all[typeID]
end

jewelrytype.TypeNameMap = {}
function jewelrytype.getByTypeName(typeName)
    return jewelrytype.TypeNameMap[typeName]
end

function jewelrytype._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelrytype:_create(os)
        if #(v.typeName) > 0 then
            jewelrytype[v.typeName] = v
        end
        jewelrytype.all[v.typeID] = v
        jewelrytype.TypeNameMap[v.typeName] = v
    end
    if jewelrytype.Jade == nil then
        errors.enumNil("equip.jewelrytype", "Jade");
    end
    if jewelrytype.Bracelet == nil then
        errors.enumNil("equip.jewelrytype", "Bracelet");
    end
    if jewelrytype.Magic == nil then
        errors.enumNil("equip.jewelrytype", "Magic");
    end
    if jewelrytype.Bottle == nil then
        errors.enumNil("equip.jewelrytype", "Bottle");
    end
end

function jewelrytype._reload(os, errors)
    local old = jewelrytype.all
    jewelrytype.all = {}
    jewelrytype._initialize(os, errors)
    for k, v in pairs(jewelrytype.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return jewelrytype
