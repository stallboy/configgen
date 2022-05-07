local pairs = pairs
local ipairs = ipairs
local tostring = tostring
local type = type
local table = table

local function is_list(val)
    local n = 0
    for _ in pairs(val) do
        n = n + 1
    end
    return n == #val
end

local function list_tostring(val)
    -- t里的value只能是bean或基本类型
    local res = {}
    local i = 1
    for _, v in ipairs(val) do
        res[i] = tostring(v)
        i = i + 1
    end
    return "[" .. table.concat(res, ",") .. "]"
end

local function map_tostring(val)
    -- t里的value只能是bean或基本类型
    local res = {}
    local i = 1
    for k, v in pairs(val) do
        res[i] = tostring(k) .. "=" .. tostring(v)
        i = i + 1
    end
    return "{" .. table.concat(res, ",") .. "}"
end

local function bean_tostring(t)
    local res = {}
    local i = 1
    for _, f in ipairs(t.Fields) do
        --- bean有Fields信息
        local val = t[f]
        local typ = type(val)
        local vstr
        if typ == 'table' then
            if val.Fields then
                vstr = tostring(val)
            elseif is_list(val) then
                vstr = list_tostring(val)
            else
                vstr = map_tostring(val)
            end
        else
            vstr = tostring(val)
        end

        res[i] = f .. '=' .. vstr
        i = i + 1
    end
    return table.concat(res, ',')
end

local init = {}

init.tostring = function(t)
    return '{' .. bean_tostring(t) .. '}'
end

init.action_tostring = function(t)
    return t.type() .. '{' .. bean_tostring(t) .. '}'
end

--- 用于设置bean，或table 的metatable.__newindex元方法，应用可自己加只读检测
init.newindex = function(t, k, v)
    print("在逻辑中修改配置数据！！！忽略， key=" .. tostring(k) .. ", value=" .. tostring(v))
end

--- EmptyTable，为减少内存占用，所有生成的配置数据共享这个，也用于空table的只读检测
local meta = {}
meta.__newindex = init.newindex
local E = {}
setmetatable(E, meta)
init.E = E

--- ReadOnly, 用于list，map类型table的只读检测
init.R = function(v)
    setmetatable(v, meta)
    return v
end

return init
