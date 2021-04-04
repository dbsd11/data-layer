package group.bison.data.data_layer.common.enums;

/**
 * Created by diaobisong on 2020/7/2.
 */
public enum SegmentTypeEnum {

    STRING, NUMERIC, DATETIME;

    public static SegmentTypeEnum getByName(String name) {
        for (SegmentTypeEnum segmentTypeEnum : values()) {
            if (segmentTypeEnum.name().equalsIgnoreCase(name)) {
                return segmentTypeEnum;
            }
        }
        return null;
    }

}
