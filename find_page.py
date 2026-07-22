import re

with open("screen.kt.txt", "r") as f:
    text = f.read()

idx = text.find("if (page == 0) {")
if idx != -1:
    count = 1
    i = text.find("{", idx) + 1
    while count > 0 and i < len(text):
        if text[i] == "{": count += 1
        elif text[i] == "}": count -= 1
        i += 1
    
    print("Ends at:", i)
    print("Next 50 chars:", text[i:i+50])
    
