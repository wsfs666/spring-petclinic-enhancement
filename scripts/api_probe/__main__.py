import argparse
from .runner import run_probes
from .report import write_jsonl
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-url',dest='base_url')
    parser.add_argument('--count',dest='count',type=int)
    parser.add_argument('--timeout',dest='timeout',type=float)
    parser.add_argument('--output')
    args = parser.parse_args()
    url = f"{args.base_url}/api/owners/9999"
    result = run_probes(url,args.count,args.timeout)
    write_jsonl(result,args.output)
   # print(result)


if __name__ == '__main__':
    main()