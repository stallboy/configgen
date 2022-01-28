local cfg = require "cfg._cfgs"
local Beans = cfg._beans

---@class cfg.task.task
---@field taskid number , 任务完成条件类型（id的范围为1-100）
---@field name table<number,text> , 程序用名字
---@field nexttask number 
---@field completecondition Beans.task.completecondition 
---@field exp number 
---@field get fun(taskid:number):cfg.task.task
---@field all table<any,cfg.task.task>
---@field NullableRefTaskid cfg.task.taskextraexp
---@field NullableRefNexttask cfg.task.task

local this = cfg.task.task

local mk = cfg._mk.table(this, { { 'all', 'get', 1 }, }, nil, { 
    { 'NullableRefTaskid', 0, cfg.task.taskextraexp, 'get', 1 }, 
    { 'NullableRefNexttask', 0, cfg.task.task, 'get', 3 }, }, 
    'taskid', -- int, 任务完成条件类型（id的范围为1-100）
    'name', -- list,text,2, 程序用名字
    'nexttask', -- int
    'completecondition', -- task.completecondition
    'exp' -- int
    )

local chat = Beans.task.completecondition.chat
local collectitem = Beans.task.completecondition.collectitem
local conditionand = Beans.task.completecondition.conditionand
local killmonster = Beans.task.completecondition.killmonster
local talknpc = Beans.task.completecondition.talknpc
local testnocolumn = Beans.task.completecondition.testnocolumn

mk(1, {"杀个怪", "杀怪"}, 2, killmonster(1, 3), 1000)
mk(2, {"和npc对话", "和npc对话"}, 3, talknpc(1), 2000)
mk(3, {"收集物品", "收集物品"}, 0, collectitem(11, 1), 3000)
mk(4, {"杀怪并且收集物品", "杀怪并且收集物品"}, 0, conditionand(killmonster(1, 3), collectitem(11, 1)), 4000)
mk(5, {"杀怪对话并且收集物品", "杀怪对话并且收集物品"}, 0, conditionand(conditionand(killmonster(1, 3), talknpc(1)), collectitem(11, 1)), 5000)
mk(6, {"聊天并且杀怪", "测试转义符号"}, 0, conditionand(chat("葵花宝典,123"), killmonster(1, 3)), 5000)
mk(7, {"测试", "测试无参数得bean"}, 0, testnocolumn, 2000)

return this
