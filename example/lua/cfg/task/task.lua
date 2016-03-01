local Beans = require("cfg._beans")

local task = {}

function task:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.taskid = os:ReadInt32() -- 任务完成条件类型（id的范围为1-100）
    o.name = os:ReadString() -- 程序用名字
    o.desc = os:ReadString() -- 注释
    o.nexttask = os:ReadInt32()
    o.completecondition = Beans.task.completecondition:_create(os)
    o.exp = os:ReadInt32()
    return o
end

function task:_assign(other)
    self.taskid = other.taskid
    self.name = other.name
    self.desc = other.desc
    self.nexttask = other.nexttask
    self.completecondition:_assign(other.completecondition)
    self.exp = other.exp
end

task.all = {}
function task.get(taskid)
    return task.all[taskid]
end

function task._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = task:_create(os)
        task.all[v.taskid] = v
    end
end

function task._reload(os, errors)
    local old = task.all
    task.all = {}
    task._initialize(os, errors)
    for k, v in pairs(task.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return task
