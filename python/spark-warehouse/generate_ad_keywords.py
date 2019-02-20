import os
import sys
import json
import random
from sets import Set

if __name__ == "__main__":
    input_file = sys.argv[1]
    ad_keyword_output_file = sys.argv[3]

    ad_id_keyword_output = open(ad_keyword_output_file, "w")


    with open(input_file, "r") as lines:
        for line in lines:
            entry = json.loads(line.strip())
            if  "title" in entry and "adId" in entry and "query" in entry:
                    title = entry["title"].lower().encode('utf-8')
                    ad_id_keyword_pair = str(entry["adId"]) + "," + title
                    ad_id_keyword_output.write(ad_id_keyword_pair)
                    ad_id_keyword_output.write('\n')


    word2vec_training.close()
    ad_id_keyword_output.close()
