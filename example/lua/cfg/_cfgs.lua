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

local function _resolve_Beans_levelrank(o, errors)
    o.RefRank = cfg.equip.rank.get(o.rank)
    if o.RefRank == nil then
        errors.refNil("LevelRank", "Rank", o.rank)
    end
end

local function _resolve_cfg_equip_jewelry(o, errors)
    _resolve_Beans_levelrank(o.lvlRank, errors)
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

local function _resolve_cfg_equip_jewelryrandom(o, errors)
    _resolve_Beans_levelrank(o.lvlRank, errors)
end

local function _resolve_cfg_loot(o, errors)
    for _, v in pairs(cfg.lootitem.all) do
        if v.lootid == o.lootid then
            table.insert(o.ListRefLootid, v)
        end
    end
end


local function _resolveAll(errors)
    for _, v in pairs(cfg.equip.jewelry.all) do
        _resolve_cfg_equip_jewelry(v, errors)
    end
    for _, v in pairs(cfg.equip.jewelryrandom.all) do
        _resolve_cfg_equip_jewelryrandom(v, errors)
    end
    for _, v in pairs(cfg.loot.all) do
        _resolve_cfg_loot(v, errors)
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

local _reload = false
local function _CSVProcessor(os)
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
    while true do
        local c = os:ReadCfg()
        if c == nil then
            break
        end
        cfgNils[c] = nil
        local cc = _get(cfg, c)
        if cc == nil then
            errors.cfgDataAdd(c)
        elseif _reload then
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

function cfg.Load(packDir, reload)
    _reload = reload
    Config.CSVLoader.Processor = _CSVProcessor
    Config.CSVLoader.LoadPack(packDir)
    return errors.errors
end

return cfg
