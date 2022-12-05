package com.google.gson;

/**
 * 探测策略
 *
 * @author baiyap
 * @date 2022-11-29 12:50:17
 */
public interface CycleHandleStrategy {

    String PLACEHOLDER_PREFIX = "$ref:";

    String REGULAR_EXPRESSION = "\\$ref:[0-9]+";

    String REPLACE_EXPRESSION = "\\$ref:";

    /**
     * 不处理, 默认策略
     */
    CycleHandleStrategy NONE = new CycleHandleStrategy() {
        @Override
        public Object handle(Object s, int l) {
            return s;
        }
    };

    /**
     * 尾切断
     */
    CycleHandleStrategy CUT = new CycleHandleStrategy() {
        @Override
        public Object handle(Object s, int l) {
            return null;
        }
    };


    /**
     * 抛出异常
     */
    CycleHandleStrategy THROW = new CycleHandleStrategy() {
        @Override
        public Object handle(Object s, int l) {
            throw new CycleReferenceException("cycle reference detected, please check it or handle with other CycleHandleStrategy.");
        }
    };

    /**
     * 占位符替换
     */
    CycleHandleStrategy PLACEHOLDER = new CycleHandleStrategy() {
        @Override
        public Object handle(Object s, int l) {
            return PLACEHOLDER_PREFIX + l;
        }
    };

    /**
     * 具体探测响应方式, 具体实现类自行实现
     *
     * @param source 循环引用对象
     * @param layer  循环引用层级
     * @return String 结果
     */
    Object handle(Object source, int layer);
}
