package configgen.define;

import configgen.util.DomUtils;
import org.w3c.dom.Element;

public class DefineOption {
    final String dataDir;
    final SplitMode splitMode; // xml定义文件拆分方式
    final UnspecifiedPkg unspecifiedPkg; // 未定义的pkg默认行为
    final boolean simplify; // 是否简化此xml内容中的include和exclude定义，默认false

    DefineOption(Element self) {
        DomUtils.permitAttributes(self, "datadir", "splitMode", "unspecifiedPkg", "simplify");

        String dataDir = self.getAttribute("datadir");
        this.dataDir = dataDir == null ? "." : dataDir;
        this.splitMode = SplitMode.readFrom(self);
        this.unspecifiedPkg = UnspecifiedPkg.readFrom(self);
        this.simplify = "true".equalsIgnoreCase(self.getAttribute("simplify"));
    }

    DefineOption() {
        this.dataDir = ".";
        this.splitMode = SplitMode.AllInOne;
        this.unspecifiedPkg = null;
        this.simplify = false;
    }

    void save(Element self) {
        self.setAttribute("datadir", dataDir);
        this.splitMode.saveTo(self);
        if (this.splitMode != SplitMode.AllInOne) {
            UnspecifiedPkg.saveTo(self, unspecifiedPkg);
            self.setAttribute("simplify", Boolean.toString(simplify));
        }
    }


    enum SplitMode {
        /** 所有bean的table的定义都在同一个xml文件中 */
        AllInOne,
        /** 基于包的分割，将bean和table按包名分割到不通文件夹下的xml中 */
        PkgBased,
        ;

        static SplitMode readFrom(Element self) {
            if (!self.hasAttribute("splitMode")) {
                return AllInOne;
            }

            String splitModeStr = self.getAttribute("splitMode");
            for (SplitMode tmp : SplitMode.values()) {
                if (tmp.name().equalsIgnoreCase(splitModeStr)) {
                    return tmp;
                }
            }
            if (!splitModeStr.isEmpty()) {
                throw new AssertionError("includeMode只支持:[AllInOne, PkgBased], 当前为：" + splitModeStr);
            }
            return AllInOne;
        }

        void saveTo(Element self) {
            self.setAttribute("splitMode", name());
        }
    }

    /** 未定义的pkg默认行为 */
    enum UnspecifiedPkg {
        Include,
        Exclude,
        ;

        static UnspecifiedPkg readFrom(Element self) {
            if (!self.hasAttribute("unspecifiedPkg")) {
                return Include;
            }

            String undefinedPkgStr = self.getAttribute("unspecifiedPkg");
            for (UnspecifiedPkg tmp : UnspecifiedPkg.values()) {
                if (tmp.name().equalsIgnoreCase(undefinedPkgStr)) {
                    return tmp;
                }
            }
            if (!undefinedPkgStr.isEmpty()) {
                throw new AssertionError("unspecifiedPkg只支持:[Include, Exclude], 当前为：" + undefinedPkgStr);
            }
            return null;
        }

        static void saveTo(Element self, UnspecifiedPkg val) {
            if (val != null) {
                self.setAttribute("unspecifiedPkg", val.name());
            }
        }
    }

    static final String commentText = "\n" +
            "splitMode：xml拆分模式，默认为AllInOne，有以下两种方式拆分\n" +
            "\tAllInOne: \n" +
            "\t\t不拆分，所有bean和table定义文件同意放到此xml中\n" +
            "\tPkgBased：\n" +
            "\t\t按照目录结构拆分，将bean和table定义分别存放到各自目录xml中(如果bean和table的就在本目录，仍然放到此xml中)，此xml会汇总个目录的定义xml，汇总方式如下：\n" +
            "\t\tinclude：\n" +
            "\t\t\t包含某个目录下的所有bean和table定义\n" +
            "\t\t\t比如：<include file=\"item\\item.xml\"/>\n" +
            "\t\texclude：\n" +
            "\t\t\t排除某个目录下所有table的定义，此时bean定义依然会被包含。\n" +
            "\t\t\t比如：<exclude file=\"shop\\shop.xml\"/>\n" +
            "\t\t注：bean本来就是抽象出来公用的，当我们想要包含或者排除某个定义时，应该只针对table，也就是真正策划填数据的文件，bean不需要显式include或exclude，最终会根据被table使用情况自动引用。\n" +
            "\n" +
            "当splitMode=PkgBased时，下面的参数才有意义\n" +
            "unspecifiedPkg：未指定的pkg默认行为，默认include\n" +
            "\tinclude: 未定义pkg默认include，加载定义信息是会扫描datadir目录下和目录同名的xml文件自动include\n" +
            "\texclude: 未定义行为默认exclude\n" +
            "\n" +
            "simplify：是否简化此xml内容中的include和exclude定义，默认false\n" +
            "\tfalse：include和exclude全部放到此xml中\n" +
            "\ttrue：undefinedPkg==include时，此xml中只会有exclude定义。 反之undefinedPkg==exclude时，此xml中只会有include定义\n";

}
