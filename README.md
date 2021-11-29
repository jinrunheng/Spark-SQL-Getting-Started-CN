## Spark SQL Getting Started 中文

[原文链接](http://spark.apache.org/docs/latest/sql-getting-started.html)

### 1. Starting Point: SparkSession

Spark 中所有功能的入口点是 SparkSession。如果要创建出一个基本的 SparkSession，只需要使用 `SparkSession.builder()` 方法：

```java
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class SparkSessionInit {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("Spark SQL Demo")
                .setMaster("local");

        SparkSession
                .builder()
                .appName("Spark SQL Demo")
                .config(conf)
                .getOrCreate();
    }
}

```

在 Spark 2.0 开始，SparkSession 为 Hive 提供了内置支持，包括使用 HiveQL 编写查询，访问 HiveUDF 以及从 Hive 表中读取数据的能力。要使用这些功能，你不需要依赖于现有的 Hive 安装程序。

### 2. Creating DataFrames

使用 SparkSession，应用程序可以从现有的 RDD，Hive 表或是 Spark 数据源中创建 DataFrames。

示例：基与 JSON 文件的内容创建一个 DataFrame

```java
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class CreateDataFrameFromJsonFile {
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

        df.show();
        // +----+-------+
        // | age|   name|
        // +----+-------+
        // |null|Michael|
        // |  30|   Andy|
        // |  19| Justin|
        // +----+-------+
    }
}

```



### 3. Untyped Dataset



