package com.github.sparksqlgettingstartedcn;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.col;

public class DataSetExample {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("Spark SQL Demo")
                .setMaster("local");

        SparkSession spark = SparkSession
                .builder()
                .appName("Spark SQL Demo")
                .config(conf)
                .getOrCreate();

        Dataset<Row> df = spark.read()
                .json("src/main/resources/people.json");

        df.printSchema();
        // root
        // |-- age: long (nullable = true)
        // |-- name: string (nullable = true)

        df.select("name").show();
        // +-------+
        // |   name|
        // +-------+
        // |Michael|
        // |   Andy|
        // | Justin|
        // +-------+
        df.select(col("name"),col("age").plus(1)).show();
        // +-------+---------+
        // |   name|(age + 1)|
        // +-------+---------+
        // |Michael|     null|
        // |   Andy|       31|
        // | Justin|       20|
        // +-------+---------+

        // Select people older than 21
        df.filter(col("age").gt(21)).show();
        // +---+----+
        // |age|name|
        // +---+----+
        // | 30|Andy|
        // +---+----+

        // Count people by age
        df.groupBy("age").count().show();
        // +----+-----+
        // | age|count|
        // +----+-----+
        // |  19|    1|
        // |null|    1|
        // |  30|    1|
        // +----+-----+
    }
}
