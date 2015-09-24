package configgen.define;

import configgen.*;
import org.w3c.dom.Element;

public class Field  extends Node{
    public final String desc;
    public final String name;
    public final String type;
    public final String ref;
    public final String nullableRef;
    public final String keyRef;
    public final String listRef;
    public final String listRefField;
    public final String range;
    public final String own;

    public Field(Bean parent, Element self) {
        super(parent, "");
        String[] attrs = Utils.attrs(self, "desc", "name", "type",
                "ref", "nullableref", "keyref", "listref", "range", "own");
        desc = attrs[0];
        name = attrs[1];
        link = "[field]" + name;
        type = attrs[2];

        ref = attrs[3];
        nullableRef = attrs[4];
        keyRef = attrs[5];
        String[] sp = attrs[6].split(",");
        listRef = sp[0];
        listRefField = sp[1];
        range = attrs[7];
        own = attrs[8];
    }

    public Field(Bean parent, String desc, String name, String type){
        super(parent, "");
        this.desc = desc;
        this.name = name;
        this.type = type;
        link = "[field]" + name;

        ref = "";
        nullableRef = "";
        keyRef = "";
        listRef = "";
        listRefField = "";
        range = "";
        own = "";
    }

}