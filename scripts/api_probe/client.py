#given a url and timeout, send a request
import requests
import time



def probe_once(url: str, timeout: float):
    before = time.perf_counter()
    
    try:
        response = requests.get(url,timeout=timeout)
        after = time.perf_counter()
        latency_ms = (after-before)*1000
        status = response.status_code
        if status>=200 and status <=399:
            success = True
            error_type = None
        elif status>=400 and status<=499:
            error_type = "http_4xx"
            success = False
        elif status>=500 and status<=599:
            error_type = "http_5xx"
            success = False
        else:
            success = False
            error_type = "unexpected_error"

    except requests.exceptions.Timeout as e:
        latency_ms = (time.perf_counter()-before)*1000
        status = None
        success = False
        error_type = "timeout"
    except requests.exceptions.ConnectionError as e:
        latency_ms = (time.perf_counter()-before)*1000
        status = None
        success = False
        error_type = "connection_error"

    

    return {"status": status,"latency_ms":latency_ms,"success":success,"error_type":error_type}



