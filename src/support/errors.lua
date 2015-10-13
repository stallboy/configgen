local errors = { cfgNils = {}, cfgDataAdds = {}, refNils = {}, enumNils = {} }

function errors.cfgDataAdd(cfg)
    table.insert(errors.cfgDataAdds, cfg)
end

function errors.cfgNil(cfg)
    table.insert(errors.cfgNils, cfg)
end

function errors.refNil(cfg, col, v)
    table.insert(errors.refNils, { cfg = cfg, col = col, v = v })
end

function errors.enumNil(cfg, ename)
    table.insert(errors.enumNils, { cfg = cfg, ename = ename })
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