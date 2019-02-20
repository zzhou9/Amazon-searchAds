import os
import sys
import json
import random
from sets import Set
from nltk.corpus import stopwords
from nltk.tokenize import wordpunct_tokenize
from nltk.stem.porter import PorterStemmer
from pprint import pprint

stop_words = set(stopwords.words('english'))
stop_words.update(['.', ',', '"', "'", '?', '!', ':', ';', '(', ')', '[', ']', '{', '}', '&','/','...','-','+','*','|',"),"])
porter = PorterStemmer()
def cleanData(input) :
    #remove stop words
    list_of_tokens = [i.lower() for i in wordpunct_tokenize(input) if i.lower() not in stop_words ]
    return list_of_tokens


if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    output = open(output_file, "w")
    url_set = Set()
    id = 2000
    with open(input_file, "r") as lines:
        for line in lines:
            entry = json.loads(line.strip())
            if "detailUrl" in entry and 'title' in entry:
                unique_id = hash(entry["title"])
                if unique_id not in url_set:
                    url_set.add(unique_id)
                    tokens = cleanData(entry["query"])
                    entry["query"] = " ".join(tokens)
                    entry["adId"] = id
                    if entry["price"] == 0.0:
                        entry["price"] = random.randint(30,480)
                    keywords = cleanData(entry["title"])
                    entry["keyWords"] = keywords
                    entry["title"] = entry["title"]
                    id += 1
                    output.write(json.dumps(entry))
                    output.write('\n')

    output.close()
