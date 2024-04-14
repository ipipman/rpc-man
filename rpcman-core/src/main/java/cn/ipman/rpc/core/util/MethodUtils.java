package cn.ipman.rpc.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/13 23:07
 */
public class MethodUtils {

    public static List<Field> findAnnotatedFiled(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        while (aClass != null) {
            // 获取实例的所有成员
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                // 判断成员是否被标记为 @Consumer 注解
                if (f.isAnnotationPresent(annotationClass)) {
                    result.add(f);
                }
            }
            // 找到被Spring容器CGLIB代理过的父类,避免找不到标记成 @RpcConsumer 的成员
            aClass = aClass.getSuperclass();
        }
        return result;
    }

    public static boolean checkLocalMethod(final String method) {
        //本地方法不代理
        return "toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method);
    }

    public static boolean checkLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String methodSign(Method method) {
        StringBuffer sb = new StringBuffer(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes())
                .forEach(
                        t -> sb.append("_").append(t.getName())
                );
        return sb.toString();
    }

}
