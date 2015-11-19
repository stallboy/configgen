local signin = {}
signin.all = {}

function signin:_create(os)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.id = os:ReadInt32() -- 礼包ID
    o.item2countMap = {} -- 普通奖励,
    for _ = 1, os:ReadSize() do
        o.item2countMap[os:ReadInt32()] = os:ReadInt32()
    end
    o.vipitem2vipcountMap = {} -- vip奖励,
    for _ = 1, os:ReadSize() do
        o.vipitem2vipcountMap[os:ReadInt32()] = os:ReadInt32()
    end
    o.viplevel = os:ReadInt32() -- 领取vip奖励的最低等级
    o.iconFile = os:ReadString() -- 礼包图标
    return o
end

function signin:_assign(other)
    self.id = other.id
    for k, v in pairs(other.item2countMap) do
        self.item2countMap[k] = v
    end
    for k, v in pairs(other.vipitem2vipcountMap) do
        self.vipitem2vipcountMap[k] = v
    end
    self.viplevel = other.viplevel
    self.iconFile = other.iconFile
end

function signin.get(id)
    return signin.all[id]
end

function signin._initialize(os, errors)
    for _ = 1, os:ReadSize() do
        local v = signin:_create(os)
        signin.all[v.id] = v
    end
end

function signin._reload(os, errors)
    local old = signin.all
    signin.all = {}
    signin._initialize(os, errors)
    for k, v in pairs(signin.all) do
        local ov = old[k]
        if ov then
            ov:_assign(v)
        end
    end
end

return signin
