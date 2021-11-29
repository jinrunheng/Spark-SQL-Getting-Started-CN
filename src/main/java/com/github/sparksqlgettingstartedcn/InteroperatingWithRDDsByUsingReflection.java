package com.github.sparksqlgettingstartedcn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import scala.Serializable;

public class InteroperatingWithRDDsByUsingReflection {
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

        JavaRDD<Person> peopleRDD = spark.read()
                .textFile("src/main/resources/people.txt")
                .javaRDD()
                .map(
                        line -> {
                            String[] parts = line.split(",");
                            Person person = new Person();
                            person.setName(parts[0]);
                            person.setAge(Integer.parseInt(parts[1].trim()));
                            return person;
                        }
                );
        // Apply a schema to an RDD of JavaBeans to get a DataFrame
        Dataset<Row> peopleDF = spark.createDataFrame(peopleRDD, Person.class);

        peopleDF.createOrReplaceTempView("people");
        Dataset<Row> teenagersDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19");

        Encoder<String> stringEncoder = Encoders.STRING();

        Dataset<String> teenagerNamesByIndexDF = teenagersDF.map(
                new MapFunction<Row, String>() {
                    @Override
                    public String call(Row row) throws Exception {
                        return "Name : " + row.getString(0);
                    }
                },
                stringEncoder
        );
        teenagerNamesByIndexDF.show();
        // +------------+
        // |       value|
        // +------------+
        // |Name: Justin|
        // +------------+

        // or by field name
        Dataset<String> teenagerNamesByFieldDF = teenagersDF.map(
                new MapFunction<Row, String>() {
                    @Override
                    public String call(Row row) throws Exception {
                        return "Name : " + row.getAs("name");
                    }
                },
                stringEncoder
        );

        teenagerNamesByFieldDF.show();
        // +------------+
        // |       value|
        // +------------+
        // |Name: Justin|
        // +------------+
    }
}
