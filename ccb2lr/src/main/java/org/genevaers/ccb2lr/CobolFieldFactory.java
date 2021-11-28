package org.genevaers.ccb2lr;

public class CobolFieldFactory {

    private CobolFieldFactory() {
    }

    private static CobolField makeField(FieldType type) {
        switch (type) {
        case ALPHA:
            return new AlphanumericField();
        case ZONED:
            return new ZonedField();
        case PACKED:
            return new PackedField();
        case BINARY:
            return new BinaryField();
        case OCCURSGROUP:
            return new OccursGroup();
        case GROUP:
            return new GroupField();
        default:
            return null;
        }
    }

    public static CobolField makeNamelessFieldFrom(CobolField src) {
        CobolField trg = null;
        if (src != null) {
            if(src.getType() == FieldType.OCCURSGROUP) {
                OccursGroup otrg = (OccursGroup) makeField(FieldType.OCCURSGROUP); //Don't want endless repeats
                if(otrg != null) {
                    otrg.setTimes(((OccursGroup)src).getTimes());
                    trg = otrg;
                }
            } else {
                trg = makeField(src.getType());
            }
            if (trg != null) {
                copyNoneNameFields(src, trg);
            }
        }

        return trg;
    }

    private static void copyNoneNameFields(CobolField src, CobolField trg) {
        trg.setPicCode(src.getPicCode());
        trg.setPicType(src.getPicType());
        trg.setSection(src.getSection());
    }

    public static GroupField makeNewGroup(int times) {
        if (times > 1) {
            OccursGroup of = (OccursGroup) CobolFieldFactory.makeField(FieldType.OCCURSGROUP);
            if (of != null) {
                of.setTimes(times);
            }
            return of;
        } else {
            return (GroupField) CobolFieldFactory.makeField(FieldType.GROUP);
        }
    }

    public static CobolField makeField(String usage, String picType) {
        // Depends on usage and pic code type
        if (usage == null) {
            if (picType.equals("alpha_x")) {
                return makeField(FieldType.ALPHA);
            } else if (picType.equals("signed_precision_9")) {
                return makeField(FieldType.ZONED);
            }
        } else {
            switch (usage.toLowerCase()) {
            case "comp-3":
                return makeField(FieldType.PACKED);
            case "comp":
            case "comp-4":
            case "comp-5":
                return makeField(FieldType.BINARY);
            default:
            }
        }
        return null;
    }
}
