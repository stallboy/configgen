local cfg = {}

cfg._mk = require "common.mkcfg"
local pre = cfg._mk.pretable

cfg.ai = {}
---@type cfg.ai.ai
cfg.ai.ai = pre("cfg.ai.ai")
---@type cfg.ai.ai_action
cfg.ai.ai_action = pre("cfg.ai.ai_action")
---@type cfg.ai.ai_condition
cfg.ai.ai_condition = pre("cfg.ai.ai_condition")
cfg.equip = {}
---@type cfg.equip.ability
cfg.equip.ability = pre("cfg.equip.ability")
---@type cfg.equip.equipconfig
cfg.equip.equipconfig = pre("cfg.equip.equipconfig")
---@type cfg.equip.jewelry
cfg.equip.jewelry = pre("cfg.equip.jewelry")
---@type cfg.equip.jewelryrandom
cfg.equip.jewelryrandom = pre("cfg.equip.jewelryrandom")
---@type cfg.equip.jewelrysuit
cfg.equip.jewelrysuit = pre("cfg.equip.jewelrysuit")
---@type cfg.equip.jewelrytype
cfg.equip.jewelrytype = pre("cfg.equip.jewelrytype")
---@type cfg.equip.rank
cfg.equip.rank = pre("cfg.equip.rank")
cfg.other = {}
---@type cfg.other.drop
cfg.other.drop = pre("cfg.other.drop")
---@type cfg.other.loot
cfg.other.loot = pre("cfg.other.loot")
---@type cfg.other.lootitem
cfg.other.lootitem = pre("cfg.other.lootitem")
---@type cfg.other.monster
cfg.other.monster = pre("cfg.other.monster")
---@type cfg.other.signin
cfg.other.signin = pre("cfg.other.signin")
cfg.task = {}
---@type cfg.task.completeconditiontype
cfg.task.completeconditiontype = pre("cfg.task.completeconditiontype")
---@type cfg.task.task
cfg.task.task = pre("cfg.task.task")
---@type cfg.task.taskextraexp
cfg.task.taskextraexp = pre("cfg.task.taskextraexp")

return cfg
