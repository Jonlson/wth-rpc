package wth.rpc.redis;

public enum RedisIDKeyEnum {
    APP_VERSION("app_version", 99990),
    APP_UPGRADE("app_upgrade", 99990),
    USER("user", 99990),

    ORDER("order", 99990),

    SOCKET("socket", 99990);


    private String key = null;

    private Integer max = 0;

    RedisIDKeyEnum(String key, Integer max) {
        this.key = key;
        this.max = max;
    }

    public String getKey() {
        return key;
    }

    public Integer getMax() {
        return max;
    }
}
