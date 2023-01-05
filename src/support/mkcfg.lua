local ipairs = ipairs
local pairs = pairs
local rawget = rawget
local rawset = rawset
local setmetatable = setmetatable
local unpack = unpack or table.unpack
local require = require

---@alias text string

local mkcfg = {}

----------------------------------------
--- tostring, action_tostring, table_newindex, E, R 要在require cfg._cfgs之前初始化好
--- 注意这里默认没有方便打印日志调试的__tostring, 没有只读检测。都需要应用自己加
---
--- 用于设置bean，或table 的metatable.__tostring元方法，
mkcfg.tostring = nil
--- 用于设置多态bean（这里叫action）的metatable.__tostring元方法，如果这个没设置，默认用tostring
mkcfg.action_tostring = nil

--- 用于设置bean，或table 的metatable.__newindex元方法，应用可自己加只读检测
mkcfg.newindex = nil

--- EmptyTable，为减少内存占用，所有生成的配置数据共享这个，也用于空table的只读检测
mkcfg.E = {}

--- ReadOnly, 用于list，map类型table的只读检测
mkcfg.R = function(v)
    return v
end

----------------------------------------
--- 以下方法require cfg._cfgs之后设也可以
---
---i18n用于类型为text的字段，用于运行时切换语言。
mkcfg.i18n = {}

--- btest用于压缩bool，（把多个bool压缩成一个integer），
--- 这个正常存储模式可以压缩一行多个bool，列存储模式可以压缩一列多个bool
--- 注意默认是错的，需要bit操作，留做应用自己来做
--- 如果生成lua时不压缩，那应用不需要设置
mkcfg.btest = function(_, _)
    --- _, _参数为v, bit
    --- bit 从0开始到52
    return true  -- TODO
end

----------------------------------------
--- refs {
---     {refName, 0, dstTable, dstGetName, thisColumnIdx, [thisColumnIdx2]}, -- 最常见类型
---     {refName, 1, dstTable, dstGetName, thisColumnIdx}, --本身是list
---     {refName, 2, dstTable, dstAllName, thisColumnIdx, dstColumnIdx}, --listRef到别的表
---     {refName, 3, dstTable, dstGetName, thisColumnIdx}, --本身是map
---}
local function mkrefs(get, refs)
    for _, ref in ipairs(refs) do
        local refName, listType, dstTable, dstGetName, k1, k2 = unpack(ref)

        if listType == 2 then
            -- t[k1]不是list，但listRef到dstTable[k2]
            -- k2不能为nil
            local dstAllName = dstGetName
            get[refName] = function(t)
                local cache = {}
                local thisColumnValue = t[k1]
                local dstall = dstTable[dstAllName]
                for _, dstRow in pairs(dstall) do
                    local dstColumnValue = dstRow[k2]
                    if dstColumnValue == thisColumnValue then
                        cache[#cache + 1] = dstRow
                    end
                end
                rawset(t, refName, cache)
                return cache
            end

        elseif listType == 3 then
            -- t[k1]本身是map，map里每个value---ref--->到dstTable.dstGetName(ele)
            -- k2肯定为 nil
            get[refName] = function(t)
                --- 对map做cache，这样能避免频繁alloc，不对非容器做，是因为非容器的ref可能为nil，反正cache不住的
                local cache = {}
                for key, val in pairs(t[k1]) do
                    cache[key] = dstTable[dstGetName](val)
                end
                rawset(t, refName, cache)
                return cache
            end

        elseif listType == 1 then
            -- t[k1]本身是list，list里每个元素ele---ref--->到dstTable.dstGetName(ele)
            -- k2肯定为 nil
            get[refName] = function(t)
                --- 对list做cache，这样能避免频繁alloc，不对非容器做，是因为非容器的ref可能为nil，反正cache不住的
                local cache = {}
                for _, ele in ipairs(t[k1]) do
                    cache[#cache + 1] = dstTable[dstGetName](ele)
                end
                rawset(t, refName, cache)
                return cache
            end
        elseif k2 == nil then
            get[refName] = function(t)
                return dstTable[dstGetName](t[k1])
            end
        else
            get[refName] = function(t)
                return dstTable[dstGetName](t[k1], t[k2])
            end
        end
    end
end

local function mkbean(refs, textFields, fields)
    local get = {}
    for i, f in ipairs(fields) do
        if textFields and textFields[f] then
            --- 重写取field方法，增加一间接层, 支持2种类型，
            --- 1表示是text
            --- 2表示是list,text
            local is_list = textFields[f] == 2
            if is_list then
                get[f] = function(t)
                    local res = {}
                    for _, ele in ipairs(t[i]) do
                        local v = mkcfg.i18n[ele]
                        if v then
                            res[#res + 1] = v
                        else
                            res[#res + 1] = ""
                        end
                    end
                    return res
                end
            else
                get[f] = function(t)
                    local v = mkcfg.i18n[t[i]]
                    if v then
                        return v
                    else
                        return ""
                    end
                end
            end
        else
            --- not in textFields
            if type(f) == 'table' then
                -- 多个bool合成一个int存储，{这一个table里存了所有的bool的列名称}
                for k, ele in ipairs(f) do
                    get[ele] = function(t)
                        return mkcfg.btest(t[i], k - 1)
                    end
                end
            else
                get[f] = function(t)
                    return t[i]
                end
            end
        end
    end

    get.Fields = function()
        --- fields都小写，refs开头是Ref，NullableRef所以起名Fields不会重复
        return fields
    end

    if refs then
        mkrefs(get, refs)
    end

    return get
end

function mkcfg.bean(refs, ...)
    return mkcfg.i18n_bean(refs, nil, ...)
end

--- i18n是要支持语言切换
function mkcfg.i18n_bean(refs, textFields, ...)
    local fields = { ... }
    local get = mkbean(refs, textFields, fields)

    local I = {}
    I.__index = function(t, k)
        local g = rawget(get, k)
        if g then
            return g(t)
        end
        return nil
    end

    if mkcfg.newindex then
        I.__newindex = mkcfg.newindex
    end

    if mkcfg.tostring then
        I.__tostring = mkcfg.tostring
    end

    local mk = function(...)
        local v = { ... }
        setmetatable(v, I)  -- 一行数据t的metatable是I，最终v[k] = get[k](t)
        return v
    end
    return mk
end

function mkcfg.action(typeName, refs, ...)
    return mkcfg.i18n_action(typeName, refs, nil, ...)
end

function mkcfg.i18n_action(typeName, refs, textFields, ...)
    local fields = { ... }
    local get = mkbean(refs, textFields, fields)

    local I = {}
    -- 让v.type() 返回"killmonster"
    I.type = function()
        return typeName
    end
    I.__index = function(t, k)
        local f = rawget(I, k)
        if f then
            return f
        end

        local g = rawget(get, k)
        if g then
            return g(t)
        end
        return nil
    end

    if mkcfg.newindex then
        I.__newindex = mkcfg.newindex
    end

    if mkcfg.action_tostring then
        I.__tostring = mkcfg.action_tostring
    elseif mkcfg.tostring then
        I.__tostring = mkcfg.tostring
    end

    local mk = {}
    -- killmonster = action(...)，让killmonster.type()返回“killmonster”
    mk.type = function()
        return typeName
    end
    -- v = killmonster(1，3)
    mk.__call = function(_, ...)
        local v = { ... }
        setmetatable(v, I)
        return v
    end
    setmetatable(mk, mk)

    return mk
end

local table2required = {}
mkcfg._table2required = table2required

--- prepareTable，或叫lazyTable都行
function mkcfg.pretable(modname)
    local I = {}
    I.__index = function(t, k)
        if table2required[t] then
            --- rawget没有，第二次请求，就返回nil
            return nil
        end

        --- 第一次请求，require后会附上相应的值，然后rawget拿到
        table2required[t] = true
        require(modname)
        return rawget(t, k)
    end

    local v = {}
    setmetatable(v, I)
    return v
end

function mkcfg.table(self, uniqkeys, enumidx, refs, ...)
    return mkcfg.i18n_table(self, uniqkeys, enumidx, refs, nil, ...)
end

-------------------------------------------------------------
--- self : table
--- uniqkeys : {{allname, getname, keyidx1, keyidx2}, }
--- enumidx : nil 或者 枚举的所在的列数
--- refs : 见mkrefs注释
--- textFields : 见mkbean函数内注释
--- ... : fields，只含名字不包含类型，todo：将来考虑加类型，
--- 如果生成lua时用了packbool，此实参又是table类型，则这个table里包含了此表所有的bool类型列的名称
function mkcfg.i18n_table(self, uniqkeys, enumidx, refs, textFields, ...)
    local fields = { ... }
    self.Metadata = { fields = fields, uniqkeys = uniqkeys, enumidx = enumidx, refs = refs }
    local get = mkbean(refs, textFields, fields)
    for _, uk in ipairs(uniqkeys) do
        local allname, getname, _, k2 = unpack(uk)
        local map = {}
        self[allname] = map
        if k2 == nil then
            self[getname] = function(k)
                return map[k]
            end
        else
            --- 2个字段可以做为uniqkey，但都必须是数字，并且第一个<1亿，第二个<1万
            self[getname] = function(k, j)
                return map[k + j * 100000000]
            end
        end
    end

    local I = {}
    I.__index = function(t, k)
        local g = rawget(get, k)
        if g then
            return g(t)
        end
        return nil
    end

    if mkcfg.newindex then
        I.__newindex = mkcfg.newindex
    end

    if mkcfg.tostring then
        I.__tostring = mkcfg.tostring
    end

    local mk
    if enumidx == nil and #uniqkeys == 1 and uniqkeys[1][4] == nil then
        --- 优化
        local allname, _, k1 = unpack(uniqkeys[1])
        local all = self[allname]
        mk = function(...)
            local v = { ... }
            setmetatable(v, I)
            all[v[k1]] = v
            return v
        end

        --- add，del,只用于部分表
        --- 这里不做出错log，因为外部要自己再包装个add, del来用
        --- 外部应该有个统一的permitAddDel的地方，来声明哪些表可被扩充，这明确出来好，有个控制点。
        self._add = mk

        self._del = function(id)
            all[id] = nil
        end

    else
        local put = function(v)
            setmetatable(v, I)
            for _, uk in ipairs(uniqkeys) do
                local allname, _, k1, k2 = unpack(uk)
                local all = self[allname]
                if k2 == nil then
                    all[v[k1]] = v
                else
                    all[v[k1] + v[k2] * 100000000] = v
                end
            end
        end

        mk = function(...)
            local v = { ... }
            put(v)

            if enumidx then
                local e = v[enumidx]
                if #e > 0 then
                    self[e] = v
                end
            end
            return v
        end

        --- add，del，因为是动态添加删除，不会增加枚举
        self._add = function(...)
            local v = { ... }
            put(v)
        end

        --- del，只考虑提供删除主键的接口
        local allname, _, _, k2 = unpack(uniqkeys[1])
        local all = self[allname]
        if k2 == nil then
            self._del = function(id)
                all[id] = nil
            end
        else
            self._del = function(id1, id2)
                all[id1 + id2 * 100000000] = nil
            end
        end
    end

    return mk
end

--- 列存储模式的mkcfgc.lua会用到
mkcfg._mkrefs = mkrefs

return mkcfg
