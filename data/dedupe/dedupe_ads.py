import os
import sys
import json
import random
from sets import Set

if __name__ == "__main__":
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    output = open(output_file, "w")
    url_set = Set()
    id = 1000
    with open(input_file, "r") as lines:
        for line in lines:
            entry = json.loads(line.strip())
            if "detail_url" in entry and "title" in entry:
                unique_id = hash(entry["detail_url"])
                if unique_id not in url_set:
                    url_set.add(unique_id)
                    entry["query"] = entry["query"].lower()
                    entry["adId"] = id
                    if entry["price"] == 0.0:
                        entry["price"] = random.randint(30,480)
                    entry["keyWords"] = entry["title"].lower().split(" ")
                    id += 1
                    output.write(json.dumps(entry))
                    output.write('\n')

    output.close()
