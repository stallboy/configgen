local ability = {}
ability.attack = nil
ability.defence = nil
ability.hp = nil
ability.critical = nil
ability.critical_resist = nil
ability.block = nil
ability.break_armor = nil

function ability:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.id = os:ReadInt32() -- 属性类型
    o.name = os:ReadString() -- 程序用名字
    return o
end

function ability:_assign(other)
    self.id = other.id
    self.name = other.name
end

ability.all = {}
function ability.get(id)
    return ability.all[id]
end

function ability._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = ability:_create(os)
        if #(v.name) > 0 then
            ability[v.name] = v
        end
        ability.all[v.id] = v
    end
    if ability.attack == nil then
        errors.enumNil("equip.ability", "attack");
    end
    if ability.defence == nil then
        errors.enumNil("equip.ability", "defence");
    end
    if ability.hp == nil then
        errors.enumNil("equip.ability", "hp");
    end
    if ability.critical == nil then
        errors.enumNil("equip.ability", "critical");
    end
    if ability.critical_resist == nil then
        errors.enumNil("equip.ability", "critical_resist");
    end
    if ability.block == nil then
        errors.enumNil("equip.ability", "block");
    end
    if ability.break_armor == nil then
        errors.enumNil("equip.ability", "break_armor");
    end
end

function ability._reload(os, errors)
    local old = ability.all
    ability.all = {}
    ability._initialize(os, errors)
    for k, v in pairs(ability.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return ability
