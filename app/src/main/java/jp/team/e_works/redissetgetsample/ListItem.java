package jp.team.e_works.redissetgetsample;

public class ListItem {
    private String mKey;
    private String mValue;

    public ListItem(String key, String value) {
        mKey = key;
        mValue = value;
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mValue;
    }
}
