## Configuration

* db.datadir，splitMode
    - 数据文件所在目录，默认为当前目录.
    - splitMode可以为AllInOne，或PkgBased
      - AllInOne是指所有bean和table的定义放在单独一个文件中，一般叫config.xml
      - PkgBased是基于文件夹的分割，将bean和table分割到不同文件夹下的xml中

* bean.name，enumRef，defaultBeanName
    - bean必须自己手工在config.xml里定义；column.type包含bean的时候也必须手工指定；这些没法自动猜测。
    - name用.分割构成名字空间
    - enumRef支持动态bean，这个指向一个enum的table，在这个bean里定义子bean
    - defaultBeanName 用于多态Bean，支持此Bean在csv的对应格中空着，就自动选择此子Bean，此子Bean不能包含字段。
    
* table.name，primaryKey，isPrimaryKeySeq，enum，enumPart，extraSplit
    - name文件路径，全小写，路径用.分割。这样和package名称统一，也避免linux，windows差异
    - primary key! 默认是第一个field，如果不是请配置，逗号分割，比如keys="aa,bb"就是2个field aa,bb为key
    - 如果有isPrimaryKeySeq，则主键值必须是1,2,3,4...
    - 如果程序想访问单独一行，配置enumPart，比如enumPart="aa"就是field aa作为enum名称，enum是全枚举，如果增加不支持热更
    - extraSplit为生成lua文件时是否为数据生成多个文件，默认不。
    - 假如数据项有250行，extraSplit配置为100，则分为3个文件，会额外多出_1,_2两个文件，原文件和_1各100行，_2含50行，引入这个是因为lua生成assets.lua时报错，assets.lua是资源系统自动生成的一个文件，里面会包含很多行，因为lua单个文件不能多余65526个constant，生成lua文件会报错，所以这里分割一下
    
* column.name，type，desc，compressAsOne
    - name, desc会从csv文件第1,2行提取,生成代码时保留csv中配置名称的大小写，成员变量为首字母小写的name,引用的成员变量为Ref+首字母大写的name
    - type 在xml要自己修改.基本类型有bool,int,long,float,string(text),复合类型包括bean,list,map
        - text用于客户端实现国际化需求，所有配置为text的字段数据会被单独放入一个文件中，只要修改这个文件就自动起作用了。
        - list,xx,count     ArrayList；compressAsOne时count可不要
        - map,xx,yy,count   LinkedHashMap;
        - 如果type里包含bean，且不是一个单元格，则要在csv里第二行名字起名为field.name@xx，同时从这开始后列名字序列不要断，要和config.xml里的定义顺序一致，方便程序检测这个field的结束点。
        
    - compressAsOne，可支持把任意嵌套bean（包括循环嵌套bean）写在一个格子里，比如a,(b1,b2),c(d,e(f,g)).转义规则同csv标准，
      比如数组里的一个字符串含有逗号，那么得用"号把它扩起来，如果引号里有引号，则要用双引号
        - "a",b,c   等同与a;b;c
        - "a,b",c   则被分为2组a,b 和c
        - "a"",b";c 也是2组a",b和c
   
* uniqueKey.keys
    - 唯一键，keys可多列，逗号分割
    - 可直接在table上配置
    
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
