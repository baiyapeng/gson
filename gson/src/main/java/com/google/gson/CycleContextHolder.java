package com.google.gson;


import com.google.gson.stream.JsonWriter;
import org.openjdk.jol.vm.VM;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * 探测对象上下文容器
 *
 * @author baiyap
 * @date 2022-11-29 12:50:17
 */
public class CycleContextHolder {

    private static CycleHandleStrategy cycleHandleStrategy = CycleHandleStrategy.NONE;

    private static ThreadLocal<Deque<Long>> stack = new ThreadLocal<>();

    public static boolean handle(JsonWriter out, Object obj) throws IOException {
        Integer layer = contains(obj);
        if (layer != null) {
            Object handle = cycleHandleStrategy.handle(obj, layer);
            if (handle == null) {
                out.nullValue();
                return true;
            }
            if (handle != obj && handle.getClass() == String.class) {
                out.value(handle.toString());
                return true;
            }
        }
        return false;
    }

    static void setCycleHandleStrategy(CycleHandleStrategy cycleHandleStrategy) {
        CycleContextHolder.cycleHandleStrategy = cycleHandleStrategy;
    }

    public static void push(long address) {
        Deque<Long> deque = stack.get();
        if (deque == null) {
            deque = new ArrayDeque<>();
            stack.set(deque);
        }
        deque.push(address);
        // System.out.println(Arrays.toString(deque.toArray(new Long[0])));
    }

    public static void push(Object obj) {
        // System.out.println(obj.getClass().getName());
        push(addressOf(obj));
    }

    public static void pop() {
        Deque<Long> deque = stack.get();
        if (deque != null) {
            deque.pop();
        }
    }

    public static void clear() {
        stack.remove();
    }

    public static Integer contains(long address) {
        Deque<Long> deque = stack.get();
        if (deque == null) {
            return null;
        }
        Long[] array = deque.toArray(new Long[0]);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == address) {
                return i;
            }
        }
        return null;
    }

    public static Integer contains(Object obj) {
        return contains(addressOf(obj));
    }

    private static long addressOf(Object obj) {
        Objects.requireNonNull(obj);
        return VM.current().addressOf(obj);
    }
}
