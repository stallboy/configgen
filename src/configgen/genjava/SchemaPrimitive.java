package configgen.genjava;

public enum SchemaPrimitive implements Schema {
    SBool {
        @Override
        public void write(ConfigOutput output) {
            output.writeInt(BOOL);
        }
    },
    SInt {
        @Override
        public void write(ConfigOutput output) {
            output.writeInt(INT);
        }
    },
    SLong {
        @Override
        public void write(ConfigOutput output) {
            output.writeInt(LONG);
        }
    },
    SFloat {
        @Override
        public void write(ConfigOutput output) {
            output.writeInt(FLOAT);
        }
    },
    SStr {
        @Override
        public void write(ConfigOutput output) {
            output.writeInt(STR);
        }
    };

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T accept(VisitorT<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean compatible(Schema other) {
        return this == other;
    }
}
