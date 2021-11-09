package org.genevaers.ccb2lr;

import java.util.Iterator;

import org.genevaers.ccb2lr.CobolField.FieldType;

public class CobolFieldFactory {

    private CobolFieldFactory() {
    }

    public static CobolField makeField(FieldType type) {
        switch (type) {
        case ALPHA:
            return new AlphanumericField();
        case ZONED:
            return new ZonedField();
        case PACKED:
            return new PackedField();
        case BINARY:
            return new BinaryField();
        case GROUP:
            return new GroupField();
        case RECORD:
            return new RecordField();
        default:
            return null;
        }
    }

    public static CobolField makeNamedFieldFrom(CobolField src) {
        CobolField trg = null;
        if (src != null) {
            switch (src.getType()) {
            case ALPHA:
                trg = new AlphanumericField();
                break;
            case ZONED:
                trg = new ZonedField();
                break;
            case PACKED:
                trg = new PackedField();
                break;
            case BINARY:
                trg = new BinaryField();
                break;
            case GROUP:
                trg = new GroupField();
                break;
            case RECORD:
                trg = new RecordField();
                break;
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
        trg.setParent(src.getParent());
    }

    public static GroupField copyGroupWith(GroupField group, int t) {
        GroupField newGroup = (GroupField) makeField(FieldType.GROUP);
        String newGroupName = group.getName() + String.format("-%02d", t);
        newGroup.setName(newGroupName);
        Iterator<CobolField> oi = group.getFieldIterator();
        while(oi.hasNext()) {
            CobolField of = oi.next();
            String newName = of.getName() + String.format("-%02d", t);
            CobolField newField = CobolFieldFactory.makeNamedFieldFrom(of);
            newField.setName(newName);
            newGroup.addField(newField);
        }
        return newGroup;
    }
}
