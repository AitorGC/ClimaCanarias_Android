def get_block(s, start):
    idx = s.find("{", start)
    if idx == -1: return -1
    count = 1
    idx += 1
    while count > 0 and idx < len(s):
        if s[idx] == "{": count += 1
        elif s[idx] == "}": count -= 1
        idx += 1
    return idx
with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()
hp_start = s.find("HorizontalPager(")
hp_block_start = s.find("{", hp_start)
hp_block_end = get_block(s, hp_start)
print(s[hp_block_end:hp_block_end+500])
