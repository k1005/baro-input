"""Capture the connected Android test device; optionally tap an observed label."""
import pathlib
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ADB = r"C:/Users/ABC/AppData/Local/Android/Sdk/platform-tools/adb.exe"

def adb(*args):
    return subprocess.check_output([ADB, *args])

def dump():
    adb("shell", "uiautomator", "dump", "/sdcard/baro-audit.xml")
    return adb("shell", "cat", "/sdcard/baro-audit.xml")

if len(sys.argv) > 2:
    nodes = [n for n in ET.fromstring(dump()).iter("node") if n.get("text") == sys.argv[2] and n.get("clickable") == "true"]
    if len(nodes) != 1:
        raise RuntimeError(f"Expected one visible control: {sys.argv[2]}, got {len(nodes)}")
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", nodes[0].get("bounds")))
    adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
xml = dump()
path = pathlib.Path(__file__).parent / sys.argv[1]
path.with_suffix(".xml").write_bytes(xml)
path.with_suffix(".png").write_bytes(adb("exec-out", "screencap", "-p"))
print("\n".join(f"{n.get('class').split('.')[-1]}: {n.get('text')} {n.get('bounds')}" for n in ET.fromstring(xml).iter("node") if n.get("text")))
