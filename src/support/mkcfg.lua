local ipairs = ipairs
local pairs = pairs
local rawget = rawget
local setmetatable = setmetatable
local unpack = unpack
local require = require

---@alias text string

local mkcfg = {}
mkcfg.tostring = nil
mkcfg.action_tostring = nil

local btest = function(v, bit)
    ---bit 从0开始到52
    return true  -- TODO
end

local bint = function(v, bitLow, bitCount)
    --- bitLow从0, bitCount最大26, bitLow+bitCount最大53
    return v -- TODO
end

mkcfg.i18n = {}

mkcfg.E = {} --- emptyTable，为减少内存占用，所有生成的配置数据共享这个，代码别改哦
mkcfg.R = function(v)
    --- ReadOnly
    return v -- Note: 可设置__newindex进行检测
end

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
                t[refName] = cache
                return cache
            end

        elseif listType == 3 then
            -- t[k1]本身是map，map里每个value---ref--->到dstTable.dstGetName(ele)
            -- k2肯定为 nil
            get[refName] = function(t)
                --- 只对map做cache，这样能避免频繁alloc，不对非容器做，是因为非容器的ref可能为nil，反正cache不住的
                local cache = {}
                for key, val in pairs(t[k1]) do
                    cache[key] = dstTable[dstGetName](val)
                end
                t[refName] = cache
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
                t[refName] = cache
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
                -- 多个bool合成一个int存储
                for k, ele in ipairs(f) do
                    get[ele] = function(t)
                        return btest(t[i], k - 1)
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

--- uniqkeys : {{allname, getname, keyidx1, keyidx2}, }
function mkcfg.i18n_table(self, uniqkeys, enumidx, refs, textFields, ...)
    local fields = { ... }
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
            --- 2个字段可以做为uniqkey，但都必须是数字，并且第一个<1千万，第二个<10万
            self[getname] = function(k, j)
                return map[k + j * 10000000]
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
    else
        mk = function(...)
            local v = { ... }
            setmetatable(v, I)
            for _, uk in ipairs(uniqkeys) do
                local allname, _, k1, k2 = unpack(uk)
                local all = self[allname]
                if k2 == nil then
                    all[v[k1]] = v
                else
                    all[v[k1] + v[k2] * 10000000] = v
                end
            end

            if enumidx then
                local e = v[enumidx]
                if #e > 0 then
                    self[e] = v
                end
            end
            return v
        end
    end
    return mk
end




-- 列模式存储的table ----------------------------------------------
-- TODO ref这有变化，列模式应该不匹配了，用的时候需要改


--- refs { {refname, islist, dsttable, dstgetname, key1, key2}, }
local function mkbeanc(self, refs, textFields, fields)
    local get = {}
    for i, f in ipairs(fields) do
        if textFields and textFields[f] then
            -- 重写取field方法，增加一间接层, 支持2种类型，<1>true表示是text，<2>2表示是list,text
            local is_list = textFields[f] == 2
            if is_list then
                get[f] = function(t)
                    local row_idx = t[1]
                    local val = self.rawall[i][row_idx]
                    local res = {}
                    for ei, ele in ipairs(val) do
                        local v = mkcfg.i18n[ele]
                        if v then
                            res[ei] = v
                        else
                            res[ei] = ""
                        end
                    end
                    return res
                end
            else
                get[f] = function(t)
                    local row_idx = t[1]
                    local val = self.rawall[i][row_idx]
                    local v = mkcfg.i18n[val]
                    if v then
                        return v
                    else
                        return ""
                    end
                end
            end
        else
            -- 不是国际化字段
            if type(f) == 'table' then
                -- 有压缩哦
                local fn = f[1]
                local isInt = f[2]
                local bitLen = 1
                if isInt then
                    bitLen = f[3]
                end
                local countPerOne = math.floor(53 / bitLen)

                if isInt then
                    get[fn] = function(t)

                        local row_idx = t[1] - 1
                        local idx = math.floor(row_idx / countPerOne)
                        local packed = self.rawall[i][idx + 1]
                        local idx_in_packed = row_idx - idx * countPerOne

                        return bint(packed, idx_in_packed, bitLen)
                    end
                else
                    get[fn] = function(t)
                        local row_idx = t[1] - 1
                        local idx = math.floor(row_idx / 53)
                        local packed = self.rawall[i][idx + 1]
                        local idx_in_packed = row_idx - idx * 53
                        return btest(packed, idx_in_packed)
                    end
                end

            else
                get[f] = function(t)
                    local row_idx = t[1]
                    return self.rawall[i][row_idx]
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

function mkcfg.tablec(self, uniqkeys, enum, refs, ...)
    return mkcfg.i18n_tablec(self, uniqkeys, enum, refs, nil, ...)
end

--- uniqkeys : {{allname, getname, key1, key2}, }
function mkcfg.i18n_tablec(self, uniqkeys, enum, refs, textFields, ...)
    local fields = { ... }
    local get = mkbeanc(self, refs, textFields, fields)
    -- 设置访问函数
    for _, uk in ipairs(uniqkeys) do
        local allname, getname, _, k2 = unpack(uk)
        local map = {}
        self[allname] = map
        if k2 == nil then
            self[getname] = function(k)
                return map[k]
            end
        else
            --- 2个字段可以做为uniqkey，但都必须是数字，并且第一个<1千万，第二个<10万
            self[getname] = function(k, j)
                return map[k + j * 10000000]
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

    if mkcfg.tostring then
        I.__tostring = mkcfg.tostring
    end

    local mk

    mk = function(row_cnt, vs)
        self.rawall = vs

        local rows = {}
        for r = 1, row_cnt do
            local rd = { r }
            setmetatable(rd, I)
            rows[r] = rd
        end

        for _, uk in ipairs(uniqkeys) do
            local allname, _, k1, k2 = unpack(uk) -- k1, k2，这里要是字符串
            local all = self[allname]

            for _, rd in ipairs(rows) do
                if k2 == nil then
                    all[rd[k1]] = rd
                else
                    all[rd[k1] + rd[k2] * 10000000] = rd
                end
            end
        end

        if enum then
            -- enumidx　要是字符串
            for _, rd in ipairs(rows) do
                local e = rd[enum]
                if #e > 0 then
                    self[e] = rd
                end
            end
        end
    end

    return mk
end

return mkcfg
