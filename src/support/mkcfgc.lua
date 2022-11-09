local ipairs = ipairs
local rawget = rawget
local setmetatable = setmetatable
local unpack = unpack or table.unpack
local require = require

--------------------------- 列模式存储的table ------------------------
-- 用列存储模式时，再require cfg._cfg前，require这个文件就行了
-- TODO ref这有变化，列模式应该不匹配了，用的时候需要改

local mkcfg = require("common.mkcfg")

local mkrefs = mkcfg._mkrefs

--- bint用于压缩int。（把多个int压缩成一个integer）
--- 只应用于列存储模式
mkcfg.bint = function(v, _, _)
    --- _, _参数为bitLow, bitCount
    --- bitLow从0, bitCount最大26, bitLow+bitCount最大53
    return v -- TODO
end

--- refs { {refname, islist, dsttable, dstgetname, key1, key2}, }
function mkcfg._mkbeanc(self, refs, textFields, fields)
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

                        return mkcfg.bint(packed, idx_in_packed, bitLen)
                    end
                else
                    get[fn] = function(t)
                        local row_idx = t[1] - 1
                        local idx = math.floor(row_idx / 53)
                        local packed = self.rawall[i][idx + 1]
                        local idx_in_packed = row_idx - idx * 53
                        return mkcfg.btest(packed, idx_in_packed)
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
    local get = mkcfg.mkbeanc(self, refs, textFields, fields)
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
                    all[rd[k1] + rd[k2] * 100000000] = rd
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