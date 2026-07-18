import urllib.request
import json

req = urllib.request.Request("https://opendata.aemet.es/opendata/api/avisos_cap/ultimoelaborado/area/65?api_key=wrong_key", headers={"User-Agent": "Mozilla/5.0"})
try:
    urllib.request.urlopen(req)
except Exception as e:
    print(e.read().decode('utf-8'))
