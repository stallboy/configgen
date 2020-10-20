## FAQ

### 使用流程是怎么样的?

1. 新建或修改csv文件，csv文件前2行为header，第一行是desc，第二行是name
2. 使用configgen.jar 来生成或完善config.xml
3. 如果config.xml不满足需求，则手动修改config.xml，比如修改type，主键，增加唯一键，外键，枚举，取值约束
4. 重复1，2，3流程(可参照example目录)

### 为什么支持枚举(enum，enumPart)？

当有一个知识策划，程序都要了解的时候，放到csv里。程序也不用写魔数了。
有时候允许部分设置是很方便的，比如掉落表，一般用id索引，但有些行如果能配置enumPart导出引用，则程序会方便的多。
在实现上，java中如果enumPart则用静态成员，如果enum则生成枚举类；

### 为什么要支持可空引用(nullableRef)？

这里约定ref是必须有引用的，nullableRef是可为null的，生成代码时用前缀ref，nullableRef来做区别，逻辑使用refXx就不用检测是否为null了。

csv单元格中不填的话默认为false,0,""，所以不要用0作为一行的id。
如果有nullableRef请不要填0，请用留空。否则程序会检测报错
      
### 嵌套结构，多态结构支持？

可以直接嵌套任意层

可以通过ref,nullableRef,listRef间接嵌套，listRef特别利于把逻辑上有很多列的表（包含list<一个Bean>）
展开到另一个表中变成很多行（每个行代表一个Bean）

可以在Bean下定义多个子Bean支持多态 比如CompleteTaskCond，有Level 5, KillMonster 1001 3这样的2个子Bean。
则这个Bean所占列数是所有子Bean占列数的最大值。

如果一个column的bean是出现无法计算列数的循环嵌套，则必须配置compressAsOne
比如CompleteTaskCond有子Bean：CondAnd 有2个column条件cond1，cond2，是CompleteTaskCond，则这两个column需要配置为compressAsOne
具体配置例子：CondAnd Level(5) KillMonster(1001,3)

### 国际化策略？

对于需要国际化的字段，把类型从原来的string修改为text，使用-gen i18n 会提取标记为text类型的所有数据到../i18n/i18n-config.csv
然后在这个csv里翻译，之后再次gen时使用 -i18nfile ../i18n/i18n-config.csv 参数，这样生成文件会直接包含国际化文本

客户端支持多国语言实时切换，服务器可支持多国语言的玩家同一个服务器。使用-langSwitchDir
