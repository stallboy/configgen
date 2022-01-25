require("cfg._beans")
local cfg = require("cfg._cfgs")

local function testAllAndGet()
    local rawGet = cfg.task.task.all[1]
    local get = cfg.task.task.get(1)
    assert(rawGet == get, "主键为key，存储在all这个哈希表中，通过函数get(k)取到一行")
end

local function testMultiColumnAsPrimaryKeyGet()
    local t = cfg.other.lootitem.get(2, 40007)
    assert(t.lootid == 2, "主键可以是2个字段，get(k1, k2)")
    assert(t.itemid == 40007)
end

local function testUniqueKeyGet()
    local t = cfg.other.signin.get(1)
    local ut = cfg.other.signin.getByIdViplevel(1, 0)
    assert(ut == t, "可以有主键，也可以有唯一键，接口为getByXxx")
end

local function testField()
    local t = cfg.task.task.get(1)
    print(t.taskid, t.nexttask, t.name[1])

    assert(t.taskid == t[1], "虽然内部存储用的是array, 但通过metatable，可以用t.xxx来访问");
    assert(t.nexttask == t[3]);
    assert(t.name[1] == "杀个怪");
    assert(#t.name == 2, "task.name is list");
end

local function testListField()
    local t = cfg.task.task.get(1)
    assert(#t.name == 2, "支持列表list");
    assert(t.name[1] == "杀个怪");
end

local function testMapField()
    local t = cfg.other.signin.get(4)
    local cnt = 0
    for k, v in pairs(t.item2countMap) do
        --print(k, v)
        cnt = cnt + 1
    end

    assert(cnt == 3, "支持字典map")
    assert(t.item2countMap[10001] == 5)
end

local function testDynamicBeanField()
    local t = cfg.task.task.get(1)
    print(t.completecondition.type(), t.completecondition.monsterid, t.completecondition.count)

    assert(t.completecondition.type() == "KillMonster", "多态bean有额外加入type()方法，返回字符串")
    assert(t.completecondition.monsterid == 1, "monsterid")
    assert(t.completecondition.count == 3, "count")
end

local function testRefNotCache()
    local t = cfg.task.task.get(1)
    local refM = t.completecondition.RefMonsterid
    assert(rawget(t.completecondition, "RefMonsterid") == nil, "Ref不会缓存，rawget一直拿到的都是nil，内存小点")
end

local function testRef()
    local t = cfg.task.task.get(1)
    local rawGet = cfg.other.monster.get(t.completecondition.monsterid)
    assert(rawGet == t.completecondition.RefMonsterid, "Ref可以直接拿到另一个表的一行，不需要再去get")
end

local function testListRef()
    local t = cfg.other.loot.get(2)
    assert(rawget(t, "ListRefLootid") == nil, "listRef 会缓存起来，第一次是nil")
    print(t.name, t.lootid, #t.ListRefLootid, t.ListRefLootid[1].itemid, t.ListRefLootid[2].itemid)

    assert(#t.ListRefLootid == 7, "t.ListRefLootid")
    assert(t.ListRefLootid[1].itemid == 40007, "t.ListRefLootid[1].itemid")
    assert(t.ListRefLootid[2].itemid == 40010, "t.ListRefLootid[2].itemid")

    assert(rawget(t, "ListRefLootid") ~= nil, "listRef 会缓存起来，取过一次之后就可以直接rawget了")
end

local function testEnum()
    local t = cfg.task.completeconditiontype
    print(t.KillMonster.id, t.KillMonster.name, t.Chat.name, t.CollectItem.name)
    assert(t.get(1) == t.KillMonster)
    assert(t.KillMonster.id == 1, "配置为枚举，可以直接completeconditiontype.KillMonster访问，不用字符串")
    assert(t.KillMonster.name == "KillMonster")
end

local function testEntry()
    local t = cfg.equip.equipconfig.Instance
    local gt = cfg.equip.equipconfig.get("Instance")
    print(t.broadcastid, t.draw_protect_name)
    assert(t.broadcastid == 9500, "配置为入口，也可以直接tequipconfig.Instance访问，不用字符串")
    assert(gt == t, "entry")
end

local function testCsvColumnMode()
    local t = cfg.equip.equipconfig.Instance2
    assert(t.week_reward_mailid == 33, "csv可以一列一列配置，而不用一行一行")
end

testAllAndGet()
testMultiColumnAsPrimaryKeyGet()
testUniqueKeyGet()

testField()
testListField()
testMapField()
testDynamicBeanField()

testRef()
testRefNotCache()
testListRef()

testEnum()
testEntry()

testCsvColumnMode()
