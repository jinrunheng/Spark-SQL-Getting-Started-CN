package com.github.sparksqlgettingstartedcn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Serializable;

import java.util.ArrayList;
import java.util.List;

public class ProgrammaticallySpecifyingTheSchema {
    public static class Person implements Serializable {
        private String name;
        private long age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getAge() {
            return age;
        }

        public void setAge(long age) {
            this.age = age;
        }
    }

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("Spark SQL Demo")
                .setMaster("local");

        SparkSession spark = SparkSession
                .builder()
                .appName("Spark SQL Demo")
                .config(conf)
                .getOrCreate();

        JavaRDD<String> peopleRDD = spark.sparkContext()
                .textFile("src/main/resources/people.txt", 1)
                .toJavaRDD();

        String schemaString = "name age";
        List<StructField> fields = new ArrayList<>();
        for (String fieldName : schemaString.split(" ")) {
            StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
            fields.add(field);
        }

        StructType schema = DataTypes.createStructType(fields);

        // Convert records of the RDD (people) to Rows
        JavaRDD<Row> rowRDD = peopleRDD.map(new Function<String, Row>() {
            @Override
            public Row call(String record) throws Exception {
                String[] attributes = record.split(",");
                return RowFactory.create(attributes[0], attributes[1].trim());
            }
        });


        // Apply the schema to the RDD
        Dataset<Row> peopleDataFrame = spark.createDataFrame(rowRDD, schema);

        // Creates a temporary view using the DataFrame
        peopleDataFrame.createOrReplaceTempView("people");

        // SQL can be run over a temporary view created using DataFrames
        Dataset<Row> results = spark.sql("SELECT name FROM people");

        // The results of SQL queries are DataFrames and support all the normal RDD operations
        // The columns of a row in the result can be accessed by field index or by field name
        Dataset<String> namesDS = results.map(
                (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
                Encoders.STRING());
        namesDS.show();
        // +-------------+
        // |        value|
        // +-------------+
        // |Name: Michael|
        // |   Name: Andy|
        // | Name: Justin|
        // +-------------+
    }
}
