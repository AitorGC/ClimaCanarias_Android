#!/bin/bash
API_KEY="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaXRvcmdjODlAZ21haWwuY29tIiwianRpIjoiODIzZGJkODAtNTdjNC00NGJiLWFjMzItMDA2OWE3ZWU2MmJkIiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE3ODQwMjc0MTIsInVzZXJJZCI6IjgyM2RiZDgwLTU3YzQtNDRiYi1hYzMyLTAwNjlhN2VlNjJiZCIsInJvbGUiOiIifQ.mlpv3KmnxAWacOIiYqrfM2VCxuzcOyA8s-ItTNwYroc"
curl -s "https://opendata.aemet.es/opendata/api/avisos/hoy?api_key=$API_KEY"
curl -s "https://opendata.aemet.es/opendata/api/avisos/cap/hoy?api_key=$API_KEY"
curl -s "https://opendata.aemet.es/opendata/api/avisos/cap/actual?api_key=$API_KEY"
