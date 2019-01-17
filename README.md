# configgen

配置生成工具（从relational csv 到 只读object的mapping工具）

## 项目概况

* 通过配置外键，取值范围，使策划可以随时检测数据一致性
* 通过生成代码，使程序方便访问类型化数据，外键引用和枚举得到一行，支持java,cSharp,lua


## 快速开始
* example/config 下是csv；config.xml 是配置文件；example/java, cs, lua目录下文件是gen.bat生成的代码文件。

## 使用流程

1. 新建或修改csv文件，csv文件前2行为header，第一行是desc，第二行是name
2. 使用configgen.jar 来生成或完善config.xml
3. 如果config.xml不满足需求，则手动修改config.xml，比如修改type，主键，增加唯一键，外键，枚举，取值约束
4. 重复1，2，3流程

## 配置描述

* bean.name，compress，enumRef
    - bean必须自己手工在config.xml里定义；column.type包含bean的时候也必须手工指定；这些没法自动猜测。
    - name用.分割构成名字空间
    - compress如果要把bean整个放到一个单元格里，配置一个分隔符，默认推荐用分号;
    - enumRef支持动态bean，这个指向一个enum的table，在这个bean里定义子bean
* table.name，primaryKey, isPrimaryKeySeq，enum, enumPart
    - name文件路径，全小写，路径用.分割。这样和package名称统一，也避免linux，windows差异
    - primary key! 默认是第一个field，如果不是请配置，逗号分割，比如keys="aa,bb"就是2个field aa,bb为key
    - 如果有isPrimaryKeySeq，则主键值必须是1,2,3,4...
    - 如果程序想访问单独一行，配置enumPart，比如enumPart="aa"就是field aa作为enum名称，enum是全枚举，如果增加不支持热更
* column.name，desc, compress
    - configgen.jar会从csv文件第1,2行提取
    - list a1,a2: name为aList
    - map a1,b1,a2,b2: name为a2bMap
    - 生成代码时保留csv中配置名称的大小写，成员变量为首字母小写的name
    - 引用的成员变量为Ref+首字母大写的name
    - compress，对list，count为0的时候，配置这个，用于分隔符
* column.type
    - bool,int,long,float,string(text),bean
    - list,xx,count     ArrayList；count不存在的时候，单元格是list；count存在的话从多列fieldname1，fieldname2。。。中读取数据。
    - map,xx,yy,count   LinkedHashMap; ;xx,yy都为基本格式。从多列fieldkey1，fieldvalue1，fieldkey2, fieldvalue2。。。中读取数据。

    - csv一个单元格,可以是基本类型，也可以是bean或者list，这时要配置compress分隔符
    - text用于客户端实现国际化需求，所有配置为text的字段数据会被单独放入一个文件中，只要修改这个文件就自动起作用了。
    - bean,list通过分号;进行分隔，例如a;b;c，转义规则同csv标准，比如数组里的一个字符串含有;，那么得用"号把它扩起来，如果引号里有引号，则要用双引号
        - "a";b;c   等同与a;b;c
        - "a;b";c   则被分为2组a;b 和c
        - "a"";b";c 也是2组a";b和c
    - map, lua不支持map的key为bean。
    - 如果type里包含bean，且不是一个单元格，则要在csv里第二行名字起名为field.name@xx，同时从这开始后列名字序列不要断，要和config.xml里的定义顺序一致，方便程序检测这个field的结束点。

* uniqueKey.keys
    - 唯一键，keys可多列，逗号分割
* foreignKey.name, keys, ref, keyref, refType
    - keys 可是多列，逗号分割， 如果ref到主键只填table.name就行，如果ref到uniqueKey，则填table.name,column.name,逗号分割。keyref只针对map
    - refType默认不用填为normal，如果nullable表示可为空，如果list，则不需要ref里的column不需要是unique key
    - 如果keys是一列，可直接配置在column里
* range.key, min, max
    - 对应min,max必须两者同时都有，对数值是取值区间，对字符串是长度区间。
    - 可直接在column里配置range="min,max"逗号分割
    
* table/bean/column.own
    - 里面可填任意字符串，配合启动参数使用，contains语义。
    - 共用一份config.xml，通过启动参数own和这里的own选择生成部分，想省客户端内存用这个


## 其他

*   有哪些注意事项？

      单元格中不填的话默认为false,0,""，所以不要用0作为一行的id。
      如果有nullableRef请不要填0，请用留空。否则程序会检测报错

*   为什么使用csv？

      比xml简洁，可被版本管理系统diff，可用excel编辑。如果要用excel高级功能，请策划使用excel原始格式，导出csv

*   为什么支持enum，enumPart？

      当有一个knowledge，客户端，服务器，策划都要了解的时候，放到csv里。程序也不用写魔数了。
      有时候允许部分设置是很方便的，比如掉落表，一般用id索引，但有些行如果能配置enum导出引用，则程序会方便的多。
      所以enum在实现上，java中如果部分enum则用静态成员，如果全部enum则生成enum；c#中生成为一个静态成员。

*   为什么要支持nullableRef？

      java，c#的引用可以为null，是个设计错误，默认引用可为null妨碍了类型状态的最小化。
      这里约定ref是必须有引用的，nullableRef是可为null的，生成代码时用前缀ref，nullableRef来做区别，逻辑使用refXx就不用检测是否为null了。
      
*   嵌套Bean支持，多态Bean支持？

      可以通过ref,nullableRef,listRef间接嵌套，可以直接嵌套任意层
      通过在Bean下定义多个Bean支持多态 比如CompleteTaskCond，有Level 5, KillMonster 1001 3这样的多态Bean
      同时支持递归嵌套Bean，比如可增加CondAnd 有2个条件，这两个又都是CompleteTaskCond，配置为CondAnd Level(5) KillMonster(1001,3)

*   国际化策略？

      对于需要国际化的字段，把类型从原来的string修改为text，使用-gen i18n 会提取标记为text类型的所有数据到../i18n/i18n-config.csv
      然后在这个csv里翻译，之后再次gen时使用 -i18nfile ../i18n/i18n-config.csv 参数，这样生成文件会直接包含国际化文本

*   另一个可变列模式配置生成
      
      https://github.com/pirunxi/cfggen