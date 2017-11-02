local jewelrytype = {}
jewelrytype.Jade = nil
jewelrytype.Bracelet = nil
jewelrytype.Magic = nil
jewelrytype.Bottle = nil

function jewelrytype:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.typeName = os:ReadString() -- 程序用名字
    return o
end


jewelrytype.all = {}
function jewelrytype.get(typeName)
    return jewelrytype.all[typeName]
end

function jewelrytype._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = jewelrytype:_create(os)
        if #(v.typeName) > 0 then
            jewelrytype[v.typeName] = v
        end
        jewelrytype.all[v.typeName] = v
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

return jewelrytype
