# -*- coding: utf-8 -*-
"""Temporary 1.21.8 downgrades for the API differences listed in the notes."""
import os

ROOT = r"C:\Users\34324\AppData\Local\Temp\um-1218"

# WoodworkingWorkbenchScreen: revert MouseButtonEvent to the pre-1.21.9 signatures
p = os.path.join(ROOT, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                 "client", "screen", "WoodworkingWorkbenchScreen.java")
with open(p, encoding="utf-8-sig") as f:
    s = f.read()

s = s.replace("import net.minecraft.client.input.MouseButtonEvent;\n", "")

s = s.replace(
    """public boolean mouseClicked(MouseButtonEvent event, boolean isHovered) {
      double pMouseX = event.x();
      double pMouseY = event.y();
      int pButton = event.button();
""",
    """public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
"""
)
s = s.replace("return super.mouseClicked(event, isHovered);",
              "return super.mouseClicked(pMouseX, pMouseY, pButton);")
s = s.replace(
    """public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
      double pMouseX = event.x();
      double pMouseY = event.y();
""",
    """public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
"""
)
s = s.replace("return super.mouseDragged(event, pDragX, pDragY);",
              "return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);")

with open(p, "w", encoding="utf-8", newline="\n") as f:
    f.write(s)
print("WoodworkingWorkbenchScreen patched")

# SixSideBlock / WallSideBlock: noCollision() does not exist on 1.21.8
for name in ("SixSideBlock.java", "WallSideBlock.java"):
    bp = os.path.join(ROOT, "src", "main", "java", "com", "voxelutopia", "ultramarine",
                      "world", "block", name)
    with open(bp, encoding="utf-8-sig") as f:
        s = f.read()
    if "noOcclusion().noCollision()" in s:
        s = s.replace("noOcclusion().noCollision()", "noOcclusion()")
        with open(bp, "w", encoding="utf-8", newline="\n") as f:
            f.write(s)
        print(name, "patched")
    else:
        print(name, "already patched")
