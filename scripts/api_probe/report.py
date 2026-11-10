import json

def write_jsonl(results, path):
    with open(path, "w", encoding="utf-8") as f:
        for r in results:
            tmp = json.dumps(r)
            f.write(tmp+"\n")

  


