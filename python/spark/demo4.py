import sys
from pyspark import SparkContext

if __name__ == "__main__":
    file = sys.argv[1]

    sc = SparkContext(appName="query_ad_pair_ctr")
    data = sc.textFile(file).map(lambda line: line.strip().strip("\n").encode("utf8", "ignore").split(','))
    query_ad_pair_ctr = data.map(lambda fields :(fields[3] + "_" + fields[4], int(fields[7]))).groupByKey().map(lambda (k, values) : (k, sum(values) * 1.0 / len(values)))
    query_ad_pair_ctr.saveAsTextFile("query_ad_pair_ctr_output")

    sc.stop()
