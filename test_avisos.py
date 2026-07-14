import urllib.request
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

api_key = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaXRvcmdjODlAZ21haWwuY29tIiwianRpIjoiODIzZGJkODAtNTdjNC00NGJiLWFjMzItMDA2OWE3ZWU2MmJkIiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE3ODQwMjc0MTIsInVzZXJJZCI6IjgyM2RiZDgwLTU3YzQtNDRiYi1hYzMyLTAwNjlhN2VlNjJiZCIsInJvbGUiOiIifQ.mlpv3KmnxAWacOIiYqrfM2VCxuzcOyA8s-ItTNwYroc"

endpoints = [
    "prediccion/especifica/municipio/diaria/35016",
    "prediccion/especifica/municipio/horaria/35016",
    "avisos/cap",
    "avisos_fenomenos_adversos",
    "prediccion/nacional/hoy",
]

base_url = "https://opendata.aemet.es/opendata/api/"

for ep in endpoints:
    url = f"{base_url}{ep}?api_key={api_key}"
    try:
        req = urllib.request.Request(url, headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, context=ctx) as response:
            data = json.loads(response.read())
            print(f"Success for: {ep} -> {data.get('estado')}, {data.get('descripcion', '')}")
    except Exception as e:
        pass
