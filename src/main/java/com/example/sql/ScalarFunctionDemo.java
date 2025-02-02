package com.example.sql;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.InputGroup;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;

import com.example.model.WaterSensor;

/***
 * @author: BYDylan
 * @date: 2024-09-28 23:30:21
 * @description:
 */
public class ScalarFunctionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<WaterSensor> sensorDS =
            env.fromElements(new WaterSensor("s1", 1L, 1), new WaterSensor("s1", 2L, 2), new WaterSensor("s2", 2L, 2),
                new WaterSensor("s3", 3L, 3), new WaterSensor("s3", 4L, 4));

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Table sensorTable = tableEnv.fromDataStream(sensorDS);
        tableEnv.createTemporaryView("sensor", sensorTable);
        // 2.注册函数
        tableEnv.createTemporaryFunction("HashFunction", HashFunction.class);

        // 3.调用 自定义函数
        // 3.1 sql用法
        // tableEnv.sqlQuery("select HashFunction(id) from sensor")
        // .execute() // 调用了 sql的execute, 就不需要 env.execute()
        // .print();
        // 3.2 table api用法
        sensorTable.select(call("HashFunction", $("id"))).execute().print();
    }

    private static class HashFunction extends ScalarFunction {
        // 接受任意类型的输入, 返回 INT型输出
        public int eval(@DataTypeHint(inputGroup = InputGroup.ANY) Object o) {
            return o.hashCode();
        }
    }
}
