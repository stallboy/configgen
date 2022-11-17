package.path = './../../src/support/?.lua;' .. package.path
local mkcfg = require("mkcfg")
local init = require("mkcfginit")
package.loaded["common.mkcfg"] = mkcfg

mkcfg.tostring = init.tostring
mkcfg.action_tostring = init.action_tostring
mkcfg.newindex = init.newindex
mkcfg.E = init.E
mkcfg.R = init.R

local Beans = require("cfg._beans")
local cfg = require("cfg._cfgs")

local function testToString()
    local t1 = cfg.task.task.get(1)
    print(t1)
    local t2 = cfg.task.task.get(2)
    assert(tostring(t2) == '{taskid=2,name=[和npc对话,和npc对话],nexttask=3,completecondition=TalkNpc{npcid=1},exp=2000,testDefaultBean={testInt=22,testBool=false,testString=text,testSubBean={x=3,y=4,z=5},testList=[11,22],testList2=[3,4,5],testMap=[str in map]}}')
    --print(t2)

    local s4 = cfg.other.signin.get(4)
    --print(tostring(s4))
    assert(tostring(s4) ==
            '{id=4,item2countMap={10001=5,30002=5,30001=5},vipitem2vipcountMap={10001=10},viplevel=0,iconFile=texture/t_i10008.bundle}')

end

local function testReadOnly()
    local t1 = cfg.task.task.get(1)
    t1.xxx = 123
    assert(t1.xxx == nil)

    t1[123] = 'xxx'

    t1.testDefaultBean.yyy = 333
    t1.testDefaultBean.testList2[1] = 'add test'
    t1.testDefaultBean.testMap['add map key to empty'] = 111
end

local function testAllAndGet()
    local rawGet = cfg.task.task.all[1]
    local get = cfg.task.task.get(1)
    assert(rawGet == get, "主键为key，存储在all这个哈希表中，通过函数get(k)取到一行")
end

local function testMultiColumnAsPrimaryKeyGet()
    local t = cfg.other.lootitem.get(2, 40007)
    local all = cfg.other.lootitem.all
    assert(t.lootid == 2, "主键可以是2个int字段，get(k1, k2)")
    assert(t.itemid == 40007)
    local rawT = all[2 + 40007 * 100000000]
    assert(rawT == t, "主键是k + j * 100000000")
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
    for _, _ in pairs(t.item2countMap) do
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

local function testRef()
    local t = cfg.task.task.get(1)
    local rawGet = cfg.other.monster.get(t.completecondition.monsterid)
    assert(rawGet == t.completecondition.RefMonsterid, "Ref可以直接拿到另一个表的一行，不需要再去get")
end

local function testRefNotCache()
    local t = cfg.task.task.get(1)
    local refM = t.completecondition.RefMonsterid
    assert(refM ~= nil)
    assert(rawget(t.completecondition, "RefMonsterid") == nil, "Ref不会缓存，rawget一直拿到的都是nil，内存小点，这是个实现上的细节，将来可能会改变")
end

local function testNullableRef()
    local t = cfg.task.task.get(1)
    assert(t.nexttask == 2)
    assert(t.NullableRefNexttask == cfg.task.task.get(2), "refType=nullable，如果为空，就==nil")
    assert(t.NullableRefTaskid == cfg.task.taskextraexp.get(1))
    t = cfg.task.task.get(3)
    assert(t.NullableRefNexttask == nil)
    assert(t.NullableRefTaskid == nil)
end

local function testListRef()
    local t = cfg.other.loot.get(2)
    assert(rawget(t, "ListRefLootid") == nil, "listRef 会缓存起来，第一次是nil")
    --print(t.name, t.lootid, #t.ListRefLootid, t.ListRefLootid[1].itemid, t.ListRefLootid[2].itemid)

    assert(#t.ListRefLootid == 7, "t.ListRefLootid")
    local itemids = {}
    for _, lootitem in ipairs(t.ListRefLootid) do
        itemids[lootitem.itemid] = true
    end

    assert(itemids[40007], "t.ListRefLootid[x].itemid contains 40007")
    assert(itemids[40010], "t.ListRefLootid[x].itemid contains 40010")

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

local function testBeanAsPrimaryKeyGetError()
    local all = cfg.equip.jewelryrandom.all
    local firstK
    for k, _ in pairs(all) do
        firstK = k
        break
    end
    local t = cfg.equip.jewelryrandom.get(Beans.levelrank(5, 1))
    assert(t == nil, "bean做为主键，虽然能生成，但不好取到，因为lua的table的key如果是table，比较用的是引用比较")
    assert(cfg.equip.jewelryrandom.get(firstK) ~= nil, "只能先拿到引用")
end

local function testCsvPack()
    local all = cfg.equip.jewelryrandom.all
    local tv
    for k, v in pairs(all) do
        if k.level == 5 and k.rank == 1 then
            tv = v
            break
        end
    end
    print(tv.testPack[1].name, tv.testPack[1].range.min, tv.testPack[1].range.max,
            tv.testPack[2].name, tv.testPack[2].range.min, tv.testPack[2].range.max)
    assert(#tv.testPack == 2, "pack=1，可以把任意复杂的结构嵌入一格中")
    assert(tv.testPack[1].range.min == 100)
    assert(tv.testPack[1].range.max == 120)
    assert(tv.testPack[2].range.min == 300)
end

local function testCsvPackSep()
    local m = cfg.other.monster.get(1)
    assert(#m.posList == 3, "packSep=, 允许自定义分隔符来座分割，定义packSep的Bean只占一格，定义了packSep的column也只占一格")
    assert(m.posList[1].x == 1)
    assert(m.posList[1].y == 2)
    assert(m.posList[1].z == 3)
end

local function testExtraSplit()
    local t = cfg.equip.jewelry.get(41)
    assert(t.iD == 41, "extraSplit=40，可以生成多个lua文件组成一个表，方便更新大小最小化")
    assert(cfg.equip.jewelry.get(1).iD == 1)
    assert(cfg.equip.jewelry.get(91).iD == 91)
end

local function testCsvSplit()
    local t = cfg.other.lootitem.get(10, 20853)
    assert(t.lootid == 10, "同一个表可以放到多个csv里")
    assert(t.itemid == 20853)
end

local function testCsvCanBeExcelSheet()
    local t = cfg.ai.ai.get(10012)
    assert(t.iD == 10012, "可以读excel文件")
end

local function testCsvBlock()
    local t = cfg.other.drop.get(1)
    assert(#t.items == 4, "可以用block来配置list")
    assert(t.testmap[5] == 55, "可以用block来配置map")
    assert(#t.items[1].itemids == 3, "支持嵌套block")
end

local function testMapValueRef()
    local t = cfg.other.signin.get(4)
    assert(t.vipitem2vipcountMap[10001] == 10)
    assert(t.RefVipitem2vipcountMap[10001] == cfg.other.loot.get(10))
end

local function testDefaultBean()
    local t = cfg.task.task.get(1)
    assert(t.testDefaultBean.testInt == 0)
    assert(t.testDefaultBean.testBool == false)
    assert(t.testDefaultBean.testString == '')
    assert(t.testDefaultBean.testSubBean.x == 0)
    assert(t.testDefaultBean.testSubBean.y == 0)
    assert(#t.testDefaultBean.testList == 0)
    assert(#t.testDefaultBean.testList2 == 0)
    assert(#t.testDefaultBean.testMap == 0)
end

local function testAddDel()
    local ai = cfg.ai.ai
    local t = ai.get(999)
    assert(t == nil)
    t = ai._add(999, "召唤猴子999", "10012", -1, 10000, "10012", true)
    assert(t.iD == 999)
    assert(t.desc == "召唤猴子999")

    t = ai.get(999)
    assert(t.iD == 999)

    ai._del(999)
    t = ai.get(999)
    assert(t == nil)
end

local function testMetadata()
    local ai = cfg.ai.ai
    local fields = ai.Metadata.fields
    assert(fields[1] == "iD")
    assert(fields[2] == "desc")
    assert(fields[3] == "condID")
    assert(fields[4] == "trigTick")
    assert(fields[5] == "trigOdds")
    assert(fields[6] == "actionID")
    assert(fields[7] == "deathRemove")

    local uniqkeys = ai.Metadata.uniqkeys
    assert(#uniqkeys == 1 )
    local pk = ai.Metadata.uniqkeys[1]
    assert(pk[1] == 'all' )
    assert(pk[2] == 'get' )
    assert(pk[3] == 1 )

    local refs = cfg.equip.jewelry.Metadata.refs
    assert(#refs == 4 )

    local enumidx = cfg.equip.jewelrytype.Metadata.enumidx
    assert(enumidx == 1 )
end

testReadOnly()
testToString()

testAllAndGet()
testMultiColumnAsPrimaryKeyGet()
testUniqueKeyGet()
testBeanAsPrimaryKeyGetError()

testField()
testListField()
testMapField()
testDynamicBeanField()

testRef()
testRefNotCache()
testNullableRef()
testListRef()

testEnum()
testEntry()

testCsvColumnMode()
testCsvPack()
testCsvPackSep()
testExtraSplit()
testCsvSplit()
testCsvCanBeExcelSheet()
testCsvBlock()

testMapValueRef()

testDefaultBean()

testAddDel()
testMetadata()
print("ok")
