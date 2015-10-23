local loot = {}
loot.all = {}
loot.combo1 = nil
loot.combo2 = nil
loot.combo3 = nil

function loot._create(os)
    local o = {}
    o.lootid = os:ReadInt32() -- 序号
    o.ename = os:ReadString()
    o.name = os:ReadString() -- 名字
    o.chanceList = {} -- 掉落0件物品的概率
    for _ = 1, os:ReadSize() do
        table.insert(o.chanceList, os:ReadInt32())
    end
    o.ListRefLootid = {}
    return o
end

function loot.get(lootid)
    return loot.all[lootid]
end

function loot._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = loot._create(os)
        if #(v.ename) > 0 then
            loot[v.ename] = v
        end
        loot.all[v.lootid] = v
    end
    if loot.combo1 == nil then
        errors.enumNil("loot", "combo1");
    end
    if loot.combo2 == nil then
        errors.enumNil("loot", "combo2");
    end
    if loot.combo3 == nil then
        errors.enumNil("loot", "combo3");
    end
end

return loot