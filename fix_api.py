with open("app/src/main/java/com/example/data/WeatherApiService.kt", "r") as f:
    lines = f.readlines()

new_lines = []
in_api = False
added = False
for line in lines:
    if "interface WeatherApi {" in line:
        in_api = True
    if in_api and "}" in line and not added:
        new_lines.append("    @GET\n")
        new_lines.append("    suspend fun searchLocation(@Url url: String): GeocodingResponse\n")
        added = True
        in_api = False
    
    if "    @GET" == line.rstrip('\n') and lines.index(line) > 180:
        continue
    if "    suspend fun searchLocation(" in line and lines.index(line) > 180:
        continue
    if "        @Url url: String" in line and lines.index(line) > 180:
        continue
    if "    ): GeocodingResponse" in line and lines.index(line) > 180:
        continue
    if line.rstrip('\n') == "}" and lines.index(line) > 180:
        continue

    new_lines.append(line)

code = "".join(new_lines)
with open("app/src/main/java/com/example/data/WeatherApiService.kt", "w") as f:
    f.write(code)
