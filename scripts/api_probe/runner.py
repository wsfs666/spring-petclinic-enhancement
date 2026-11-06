from .client import probe_once



def run_probes(url, count, timeout):
    dic = []

    for i in range(count):
        tmp = probe_once(url,timeout)
        dic.append(tmp)

    return dic

