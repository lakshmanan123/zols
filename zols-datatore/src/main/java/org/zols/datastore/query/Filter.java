/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.zols.datastore.query;

/**
 *
 * @author sathish_ku
 * @param <T> type of the object for which this filter has to be applied
 */
public class Filter<T> {

    /**
     * Describes Operator for Filters
     */
    public enum Operator {

        /**
         * Search Text in Full content of Document
         */
        FULL_TEXT_SEARCH,
        /**
         * Compare with =
         */
        EQUALS,
        /**
         * Compare with
         */
        GREATER_THAN,
        /**
         * Compare with
         */
        GREATER_THAN_EQUALS,
        /**
         * Compare with
         */
        LESSER_THAN,
        /**
         * Compare with
         */
        LESSER_THAN_EQUALS,
        /**
         * Compare with Null value
         */
        IS_NULL,
        /**
         * Check not Null
         */
        IS_NOTNULL,
        /**
         * Exists in given values
         */
        EXISTS_IN,
        /**
         * Checks value in between
         */
        IN_BETWEEN
    }

    private final String name;
    private final Operator operator;
    private final Object value;

    /**
     * Intialize the Filter
     *
     * @param name name of the filter
     * @param operator operator of the filter
     * @param value value of the filter
     */
    public Filter(String name, Operator operator, T value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Intialize the Filter
     *
     * @param operator operator of the filter
     * @param value value of the filter
     */
    public Filter(Operator operator, T value) {
        this.name = null;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Intialize the Filter
     *
     * @param name name of the filter
     * @param operator operator of the filter
     * @param value
     */
    public Filter(String name, Operator operator, T... value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Intialize the Filter without value
     *
     * @param name name of the filter
     * @param operator operator of the filter
     */
    public Filter(String name, Operator operator) {
        this.name = name;
        this.operator = operator;
        this.value = null;
    }

    /**
     * gets name of the filter
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets operator of the Filter
     *
     * @return operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * gets value of the Filter
     *
     * @return value object
     */
    public Object getValue() {
        return value;
    }

}
