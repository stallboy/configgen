collectgarbage("collect")
local x = collectgarbage("count")

local cfgs = require("cfg._cfgs")
require("cfg._beans")
require("cfg._loads")

collectgarbage("collect")
local y = collectgarbage("count")
print((y - x) / 1024)


---------------------2019年12月对武林手游的配置测试结果
--method             --lua         --luajit
--old                  155.7M        99.2M
--share                106.7M        70.3M
--share,pack           102.7M        68.4M
--share,pack,col       78.4M         54.2M
--share,pack,col,nostr 59.8M         40.0M  -- 这个nostr是测试字符串占空间大小