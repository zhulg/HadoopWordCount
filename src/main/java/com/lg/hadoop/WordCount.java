package com.lg.hadoop;

/**
 * Created by zhulianggang on 2017/12/7.
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

public class WordCount {


   public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

       /**
        * IntWritable, Text 均是 Hadoop 中实现的用于封装 Java 数据类型的类，这些类实现了WritableComparable接口，
        * 都能够被串行化从而便于在分布式环境中进行数据交换，你可以将它们分别视为int,String 的替代品。
        * 声明one常量和word用于存放单词的变量
        */

        public static final IntWritable one = new IntWritable(1);
        private Text word = new Text();

          /**
         * Mapper中的map方法：
         * void map(K1 key, V1 value, Context context)
         * 映射一个单个的输入k/v对到一个中间的k/v对
         * 输出对不需要和输入对是相同的类型，输入对可以映射到0个或多个输出对。
         * Context：收集Mapper输出的<k,v>对。
         * Context的write(k, v)方法:增加一个(k,v)对到context
         * 程序员主要编写Map和Reduce函数.这个Map函数使用StringTokenizer函数对字符串进行分隔,通过write方法把单词存入word中
     * write方法存入(单词,1)这样的二元组到context中
     */

      /*    　这里有三个参数，前面两个Object key, Text value就是输入的key和value，第三个参数Context context这是可以记录输入的key和value，
       例如：context.write(word, one);此外context还会记录map运算的状态。*/

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                this.word.set(itr.nextToken());
                System.out.println("======world="+this.word.toString()+",one="+one);
                context.write(this.word, one);
            }

            System.out.println("======map over=====");
        }

    }





    public static class IntSumReduce extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        /**
         * Reducer类中的reduce方法：
         * void reduce(Text key, Iterable<IntWritable> values, Context context)
         * 中k/v来自于map函数中的context,可能经过了进一步处理(combiner),同样通过context输出
         */

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            IntWritable val;

            for (Iterator i = values.iterator(); i.hasNext(); sum += val.get()) {
                val = (IntWritable) i.next();
            }
            System.out.println("======reduce========key="+key.toString()+",sum="+sum);
            this.result.set(sum);
            context.write(key, this.result);
        }
    }

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, InterruptedException {
        //每次需要先删除
        FileUtil.deleteDir("output");
        Configuration conf = new Configuration();

        String[] otherArgs = new String[]{"input/word.txt", "output"};
        if (otherArgs.length != 2) {
            System.err.println("Usage:Merge and duplicate removal <in> <out>");
            System.exit(2);
        }

        Job job = Job.getInstance(conf, "Wordcount");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordCount.TokenizerMapper.class);
        job.setReducerClass(WordCount.IntSumReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


}