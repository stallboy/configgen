local cfg = {}
cfg.equip = {}
cfg.equip.ability = require("cfg.equip.ability")
cfg.equip.jewelry = require("cfg.equip.jewelry")
cfg.equip.jewelryrandom = require("cfg.equip.jewelryrandom")
cfg.equip.jewelrysuit = require("cfg.equip.jewelrysuit")
cfg.equip.jewelrytype = require("cfg.equip.jewelrytype")
cfg.equip.rank = require("cfg.equip.rank")
cfg.loot = require("cfg.loot")
cfg.lootitem = require("cfg.lootitem")
cfg.monster = require("cfg.monster")
cfg.signin = require("cfg.signin")
cfg.task = {}
cfg.task.completeconditiontype = require("cfg.task.completeconditiontype")
cfg.task.task = require("cfg.task.task")

local _resolve_ = {}
function _resolve_.Beans_levelrank(o, errors)
    o.RefRank = cfg.equip.rank.get(o.rank)
    if o.RefRank == nil then
        errors.refNil("LevelRank", "Rank", o.rank)
    end
end

function _resolve_.Beans_task_completecondition_killmonster(o, errors)
    o.RefMonsterid = cfg.monster.get(o.monsterid)
    if o.RefMonsterid == nil then
        errors.refNil("KillMonster", "monsterid", o.monsterid)
    end
end

function _resolve_.Beans_task_completecondition(o, errors)
    if o:type() == 'KillMonster' then
        _resolve_.Beans_task_completecondition_killmonster(o, errors)
    end
end

function _resolve_.cfg_equip_jewelry(o, errors)
    _resolve_.Beans_levelrank(o.lvlRank, errors)
    o.RefType = cfg.equip.jewelrytype.get(o.type)
    if o.RefType == nil then
        errors.refNil("equip.jewelry", "Type", o.type)
    end
    o.NullableRefSuitID = cfg.equip.jewelrysuit.get(o.suitID)
    o.RefKeyAbility = cfg.equip.ability.get(o.keyAbility)
    if o.RefKeyAbility == nil then
        errors.refNil("equip.jewelry", "KeyAbility", o.keyAbility)
    end
end

function _resolve_.cfg_equip_jewelryrandom(o, errors)
    _resolve_.Beans_levelrank(o.lvlRank, errors)
end

function _resolve_.cfg_loot(o, errors)
    for _, v in pairs(cfg.lootitem.all) do
        if v.lootid == o.lootid then
            table.insert(o.ListRefLootid, v)
        end
    end
end

function _resolve_.cfg_task_task(o, errors)
    _resolve_.Beans_task_completecondition(o.completecondition, errors)
end


local function _resolveAll(errors)
    for _, v in pairs(cfg.equip.jewelry.all) do
        _resolve_.cfg_equip_jewelry(v, errors)
    end
    for _, v in pairs(cfg.equip.jewelryrandom.all) do
        _resolve_.cfg_equip_jewelryrandom(v, errors)
    end
    for _, v in pairs(cfg.loot.all) do
        _resolve_.cfg_loot(v, errors)
    end
    for _, v in pairs(cfg.task.task.all) do
        _resolve_.cfg_task_task(v, errors)
    end
end

local errors = {}
errors.errors = { cfgNils = {}, cfgDataAdds = {}, refNils = {}, enumNils = {} }

function errors.cfgDataAdd(cfg)
    table.insert(errors.errors.cfgDataAdds, cfg)
end

function errors.cfgNil(cfg)
    table.insert(errors.errors.cfgNils, cfg)
end

function errors.refNil(cfg, col, v)
    table.insert(errors.errors.refNils, { cfg = cfg, col = col, v = v })
end

function errors.enumNil(cfg, ename)
    table.insert(errors.errors.enumNils, { cfg = cfg, ename = ename })
end

local function _get(t, namespace)
    local idx = 1
    while true do
        local start, ends = string.find(namespace, ".", idx, true)
        local subname = string.sub(namespace, idx, start and start - 1)
        local subt = t[subname]
        t = subt
        if t and start then
            idx = ends + 1
        else
            return t
        end
    end
end

cfg.Errors = errors
cfg.Reload = false
function cfg.CSVProcessor(os)
    local cfgNils = {}
    cfgNils["equip.ability"] = 1
    cfgNils["equip.jewelry"] = 1
    cfgNils["equip.jewelryrandom"] = 1
    cfgNils["equip.jewelrysuit"] = 1
    cfgNils["equip.jewelrytype"] = 1
    cfgNils["equip.rank"] = 1
    cfgNils["loot"] = 1
    cfgNils["lootitem"] = 1
    cfgNils["monster"] = 1
    cfgNils["signin"] = 1
    cfgNils["task.completeconditiontype"] = 1
    cfgNils["task.task"] = 1
    while true do
        local c = os:ReadCfg()
        if c == nil then
            break
        end
        cfgNils[c] = nil
        local cc = _get(cfg, c)
        if cc == nil then
            errors.cfgDataAdd(c)
        elseif cfg.Reload then
            cc._reload(os, errors)
        else
            cc._initialize(os, errors)
        end
    end
    for c, _ in pairs(cfgNils) do
        errors.cfgNil(c)
    end
    _resolveAll(errors)
end

function cfg.Load(packDir, behaviour, done)
    Config.CSVLoader.Processor = cfg.CSVProcessor
    Config.CSVLoader.Done = done
    Config.CSVLoader.LoadPack(packDir, behaviour)
end

return cfg
