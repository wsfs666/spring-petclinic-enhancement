import unittest
from unittest.mock import patch
from scripts.api_probe.client import probe_once
import requests

class TestApiProbe(unittest.TestCase):

##正常场景
##→ mock requests.get 返回假的 Response
    @patch('scripts.api_probe.client.requests.get')
    def test_success_response(self,mock_get):
        ##input arrange and mock
        url = "http://localhost:8080/api/owners/1"
        timeout = 0.1
        mock_get.return_value.status_code = 200

        ##Act 
        result = probe_once(url,timeout=timeout)
       

        ##assert
        self.assertEqual(result.get("status"),200)
        self.assertTrue(result.get("success"))

       



##404
##→ mock 返回 status_code=404
    @patch('scripts.api_probe.client.requests.get')
    def test_not_found_response(self,mock_get):
        url = "http://localhost:8080/api/owners/1"
        timeout = 0.1
        mock_get.return_value.status_code = 404

        result = probe_once(url,timeout=timeout)

        self.assertEqual(result.get("status"),404)
        self.assertFalse(result.get("success"))



##connection failure
##→ mock requests.get 抛 ConnectionError
    @patch('scripts.api_probe.client.requests.get')
    def test_connection_failed_response(self,mock_get):
        url = "http://localhost:8080/api/owners/1"
        timeout = 0.1
        mock_get.side_effect = requests.exceptions.ConnectionError()

        result = probe_once(url,timeout=timeout)

        self.assertEqual(result.get("status"),None)
        self.assertFalse(result.get("success"))


    

##timeout
##→ mock requests.get 抛 Timeout
    @patch('scripts.api_probe.client.requests.get')
    def test_timeout_response(self,mock_get):
        url = "http://localhost:8080/api/owners/1"
        timeout = 0.1
        mock_get.side_effect  = requests.exceptions.Timeout()

        result = probe_once(url,timeout=timeout)

        self.assertEqual(result.get("status"),None)
        self.assertFalse(result.get("success"))
