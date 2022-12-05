package com.google.gson;


import com.google.gson.stream.JsonReader;
import org.openjdk.jol.vm.VM;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 探测对象上下文容器
 *
 * @author baiyap
 * @date 2022-11-29 12:50:17
 */
public class CycleDeserializationContextHolder {

    private static final ThreadLocal<Deque<Object>> DEQUE_THREAD_LOCAL = new ThreadLocal<>();

    public static void compute(Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            long address = VM.current().addressOf(pop());
            detect(obj, obj, address);
        }
    }

    public static void detect(Object source, Object obj, long address) {
        if (isNull(obj) || isPrimitive(obj.getClass()) || isString(obj)) {
            return;
        }
        if (isArray(obj) && VM.current().addressOf(obj) == address) {
            return;
        }
        if (isArray(obj)) {
            Object[] array = (Object[]) obj;
            for (int i = 0; i < array.length; i++) {
                Object o = array[i];
                if (isNull(o) || isPrimitive(o.getClass()) || isString(o)) {
                    continue;
                }
                if (isArray(o) && VM.current().addressOf(o) == address) {
                    array[i] = source;
                    continue;
                }
                detect(source, o, address);
            }
        } else {
            List<Field> fields = getFields(obj.getClass());
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object o = field.get(obj);
                    if (isNull(o) || isPrimitive(o.getClass()) || isString(o)) {
                        continue;
                    }
                    if (isArray(o) && VM.current().addressOf(o) == address) {
                        field.set(o, source);
                        continue;
                    }
                    detect(source, o, address);
                } catch (Exception e) {

                }
            }
        }
    }

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            if (fields.length > 0) {
                list.addAll(Arrays.asList(fields));
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public static boolean isArray(Object obj) {
        return isArray(obj.getClass());
    }

    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean isPrimitive(Class<?> clazz) {
        if (clazz == Byte.class || clazz == byte.class) {
            return true;
        }
        if (clazz == Short.class || clazz == short.class) {
            return true;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return true;
        }
        if (clazz == Long.class || clazz == long.class) {
            return true;
        }
        if (clazz == Float.class || clazz == float.class) {
            return true;
        }
        if (clazz == Double.class || clazz == double.class) {
            return true;
        }
        if (clazz == boolean.class || clazz == Boolean.class) {
            return true;
        }
        if (clazz == Character.class || clazz == char.class) {
            return true;
        }
        return false;
    }

    public static boolean isString(Object obj) {
        return isString(obj.getClass());
    }

    public static boolean isString(Class<?> clazz) {
        return clazz == String.class;
    }

    public static Object handle(JsonReader in) throws IOException {
        String string = in.nextString();
        boolean match = CycleDeserializationContextHolder.match(string);
        if (match) {
            return CycleDeserializationContextHolder.contains(string);
        } else {
            throw new JsonParseException("bad json string, can not parse it.");
        }
    }

    public static void push(Object obj) {
        Deque<Object> deque = DEQUE_THREAD_LOCAL.get();
        if (deque == null) {
            deque = new ArrayDeque<>();
            DEQUE_THREAD_LOCAL.set(deque);
        }
        deque.push(obj);
    }

    public static Object pop() {
        Deque<Object> deque = DEQUE_THREAD_LOCAL.get();
        if (deque != null) {
            return deque.pop();
        }
        return null;
    }

    public static void clear() {
        DEQUE_THREAD_LOCAL.remove();
    }

    public static boolean match(String string) {
        Objects.requireNonNull(string);
        Pattern pattern = Pattern.compile(CycleHandleStrategy.REGULAR_EXPRESSION);
        return pattern.matcher(string).matches();
    }

    public static Object contains(int layer) {
        Deque<Object> deque = DEQUE_THREAD_LOCAL.get();
        if (deque == null) {
            return null;
        }
        Object[] array = deque.toArray(new Object[0]);
        return layer > array.length - 1 ? null : array[layer];
    }

    public static Object contains(String reference) {
        Objects.requireNonNull(reference);
        String replaceFirst = reference.replaceFirst(CycleHandleStrategy.REPLACE_EXPRESSION, "");
        return contains(Integer.parseInt(replaceFirst));
    }
}
