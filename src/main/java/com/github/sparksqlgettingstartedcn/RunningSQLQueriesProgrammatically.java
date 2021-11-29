package com.github.sparksqlgettingstartedcn;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class RunningSQLQueriesProgrammatically {
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

        df.createOrReplaceTempView("people");

        String sqlText = "SELECT * FROM people";
        Dataset<Row> sqlDF = spark.sql(sqlText);
        sqlDF.show();
        // +----+-------+
        // | age|   name|
        // +----+-------+
        // |null|Michael|
        // |  30|   Andy|
        // |  19| Justin|
        // +----+-------+
    }
}
