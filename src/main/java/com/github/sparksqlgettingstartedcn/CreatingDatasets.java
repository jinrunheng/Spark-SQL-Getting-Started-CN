package com.github.sparksqlgettingstartedcn;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import scala.Serializable;

import java.util.Arrays;
import java.util.Collections;

public class CreatingDatasets {

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


        Person person = new Person();
        person.setName("Andy");
        person.setAge(32);

        // Encoders are created for Java beans
        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> javaBeanDS = spark.createDataset(
                Collections.singletonList(person),
                personEncoder
        );
        // +---+----+
        // |age|name|
        // +---+----+
        // | 32|Andy|
        // +---+----+
        javaBeanDS.show();

        // Encoders for most common types are provided in class Encoders
        Encoder<Long> longEncoder = Encoders.LONG();
        Dataset<Long> primitiveDS = spark.createDataset(
                Arrays.asList(1L, 2L, 3L),
                longEncoder
        );
        Dataset<Long> transformedDS = primitiveDS.map(
                new MapFunction<Long, Long>() {
                    @Override
                    public Long call(Long value) throws Exception {
                        return value + 1;
                    }
                },
                longEncoder
        );
        transformedDS.collect(); // Returns [2,3,4]

        // DataFrames can be converted to a Dataset by providing a class. Mapping based on name
        String path = "src/main/resources/people.json";
        Dataset<Person> peopleDS = spark.read().json(path).as(personEncoder);
        peopleDS.show();
        // +----+-------+
        // | age|   name|
        // +----+-------+
        // |null|Michael|
        // |  30|   Andy|
        // |  19| Justin|
        // +----+-------+
    }
}
