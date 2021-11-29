## Spark SQL Getting Started 中文

[原文链接](http://spark.apache.org/docs/latest/sql-getting-started.html)

**所有的示例基于 Java**

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

### 3. Untyped Dataset Operations (aka DataFrame Operations)

我们来看一下 RDD，DataFrames 与 DataSet 的区别：

![Difference between DataFrame, Dataset, and RDD in Spark - Stack Overflow](https://tva1.sinaimg.cn/large/008i3skNgy1gww2lal7ssj30go0c1wfk.jpg)

如果同样的数据都给到这三种数据结构，它们分别计算之后，都会给出相同的结果，不同的是它们的执行效率和执行方式。

在后期的 Spark 版本中，DataSet 会逐步取代 RDD 与 DataFrame 称为唯一的 API 接口。

**RDD**

- RDD 是一个懒执行的不可变的可以支持 Lambda 表达式的并行数据集合
- RDD 简单，API 人性化程度高
- RDD 的劣势是性能限制，它是一个 JVM 驻内存对象，这也就决定了存在 GC 的限制和数据增加时 Java 序列化成本的升高

**DataFrame**

![111](https://tva1.sinaimg.cn/large/008i3skNgy1gww2ve4tqbj30jb0b50to.jpg)

从上图中可以直观地体现出 DataFrame 与 RDD 的区别。

左侧的 RDD[Person] 虽然以 Person 为类型参数，但 Spark 框架本身不了解 Person 类的内部结构。而右侧的DataFrame 却提供了详细的结构信息，使得 Spark SQL 可以清楚地知道该数据集中包含哪些列，每列的名称和类型各是什么。DataFrame 为数据提供了 Schema 的视图，我们可以完全将它当作数据库中的一张表来对待。

在功能上，DataFrame 除了提供了比 RDD 更丰富的算子外，更重要的特点是提升了执行效率，减少数据读取以及对执行计划的优化等等。

**DataSet**

- DataSet 是 DataFrame API 的一个扩展，是 Spark 最新的数据抽象

- 用户友好的 API 风格，既具有类型安全检查，也具有 DataFrame 的查询优化特性
- DataSet 支持编解码器，当需要访问非堆上的数据时可以避免反序列化整个对象，提高了效率
- 样例类被用来在 DataSet 中定义数据的结构信息，样例类中每个属性的名称直接映射到 DataSet 中的字段名称
- DataFrame 是 DataSet 的特例，DataFrame=DataSet[Row] ，所以可以通过 as 方法将 DataFrame 转换为DataSet。Row是一个类型，跟Car、Person这些的类型一样，所有的表结构信息都可用 Row 来表示
- DataSet 是强类型的。比如可以有 DataSet[Car]，DataSet[Person]



使用 DataSet 的结构化数据处理基本示例：

```java
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

```

### 4. Running SQL Queries Programmatically

SparkSession 上的 `sql` 函数可以使应用程序能够以编程方式运行 SQL 查询并将结果作为 DataFrame 返回。

示例：

```java
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

```

### 5. Global Temporary View

Spark SQL 中的临时视图是会话范围的，如果创建它的会话终止，它就会消失。 如果我们希望有一个在所有会话之间共享的临时视图并在 Spark 应用程序终止之前保持活动状态，可以创建一个全局临时视图。 全局临时视图与系统保留的数据库 `global_temp` 相关联，我们必须使用限定名称来引用它，例如 `SELECT * FROM global_temp.view1`。

示例：

```java
import org.apache.spark.SparkConf;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class GlobalTemporaryView {
    public static void main(String[] args) throws AnalysisException {
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

        df.createGlobalTempView("people");
        spark.sql("SELECT * FROM global_temp.people").show();
        // +----+-------+
        // | age|   name|
        // +----+-------+
        // |null|Michael|
        // |  30|   Andy|
        // |  19| Justin|
        // +----+-------+

        spark.newSession().sql("SELECT * FROM global_temp.people").show();
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

### 6. Creating Datasets

Datasets 类似于 RDD，但是，它们不使用 Java 序列化或 Kryo，而是使用专门的编码器来序列化对象，以便通过网络进行处理或传输。 虽然编码器和标准序列化都负责将对象转换为字节，但编码器是动态生成的代码，使用的格式允许 Spark 执行许多操作，如 filtering、sorting 和 hashing，而无需将字节反序列化回对象。

示例：

```java
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

```







