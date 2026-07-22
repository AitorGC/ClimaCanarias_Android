with open("app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt", "r") as f:
    s = f.read()
idx = s.find("HorizontalPager(")
end = s.find("}", idx)
# just print context after HorizontalPager
print(s[s.rfind("}", 0, s.find("}", s.find("}", s.rfind("}", 0, len(s)))))-1000 : ])
