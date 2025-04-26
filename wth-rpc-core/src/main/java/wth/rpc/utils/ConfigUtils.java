package wth.rpc.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

/**
 * 在项目一开始启动时获取配置信息.并加载到bean中。考虑拓展性，不同配置文件种类和不同生产环境
 * 但是现在为了全体适配：
 * */
public class ConfigUtils {

    public static <T> T load(Class<T> tClass, String prefix, String environment) {
        String baseName = "application";
        if (StrUtil.isNotBlank(environment)) {
            baseName += "-" + environment;
        }

        // 尝试优先找 properties，再找 yml
        String propertiesFile = baseName + ".properties";
        String yamlFile = baseName + ".yml";
        ClassPathResource properties = new ClassPathResource(propertiesFile);
        ClassPathResource yaml = new ClassPathResource(yamlFile);
        if (properties.exists()) {
            Props props = new Props(propertiesFile);
            return props.toBean(tClass, prefix);
        } else if (yaml.exists()) {
            Map<String, Object> yamlMap = YamlUtil.loadByPath(yamlFile);
            // 这里简单实现：拿到 prefix 的那一段
            tClass = getValueByPrefix(yamlMap, prefix);
            return BeanUtil.toBean(tClass, tClass);
        } else {
            throw new RuntimeException("配置文件不存在：" + propertiesFile + " 或 " + yamlFile);
        }
    }


    // 递归解析内容
    private static <T> T getValueByPrefix(Map<String, Object> map, String prefix) {
        String[] keys = prefix.split("\\.");
        if (StrUtil.isBlank(keys[0])) {
            return (T) map;
        }

        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(key);
            } else {
                return null;
            }
        }
        return (T) current;
    }
}
