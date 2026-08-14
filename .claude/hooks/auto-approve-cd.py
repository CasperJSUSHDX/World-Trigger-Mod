#!/usr/bin/env python3
"""
Claude Code PreToolUse hook: auto-approve `cd` into non-hidden subdirectories.

安全設計原則(重要,請勿隨意放寬):
1. 只認可「單純」的 cd 指令 —— 完全用 shlex 解析，指令必須恰好是
   `cd <path>`(兩個 token)。任何 &&、||、;、|、反引號、$()、換行、
   重導向符號等，一律視為「不是單純 cd」，直接放行給預設流程去問。
   這是為了避免 `cd /tmp && rm -rf ~` 這種複合指令，因為只符合
   settings.json 的 Bash(cd:*) 前綴規則而被整條放行的已知漏洞。
2. 只認可「相對路徑」的子目錄。拒絕絕對路徑(/開頭)、家目錄(~)、
   `cd -`(回上一個目錄)。
3. 拒絕路徑中任何一段是 `..`，避免用相對路徑跳出專案目錄。
4. 拒絕路徑中任何一段以 `.` 開頭(隱藏目錄，如 .git、.ssh、.env 所在的
   目錄等)，這正是使用者要求排除的情況。
5. 如果有提供 cwd，會用 realpath 解析目標路徑，確認解析後的路徑仍然
   落在 cwd 之內，防止透過 symlink 或路徑技巧繞過上面的檢查。
6. Fail-closed：任何解析失敗、格式不符預期、或邏輯有疑慮的情況，一律
   「不輸出決定」，讓 Claude Code 走回原本的詢問/規則流程 —— 這支
   hook 只會「額外放行」明確安全的 cd，絕不會「額外擋下」或「放寬」
   其他任何指令的權限。
"""

import json
import os
import shlex
import sys


def fallthrough():
    """不做任何決定，讓 Claude Code 走正常的權限流程(可能會問使用者)。"""
    sys.exit(0)


def allow(reason: str):
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "allow",
            "permissionDecisionReason": reason,
        }
    }))
    sys.exit(0)


def main():
    try:
        raw = sys.stdin.read()
        event = json.loads(raw)
    except Exception:
        fallthrough()
        return

    if event.get("tool_name") != "Bash":
        fallthrough()
        return

    command = (event.get("tool_input") or {}).get("command")
    if not isinstance(command, str) or not command.strip():
        fallthrough()
        return

    # 任何 shell 特殊字元都視為「非單純 cd」，一律不自動放行。
    # 這裡故意連引號內出現這些字元都一起擋，寧可多問一次也不要誤放行。
    danger_chars = ["&&", "||", ";", "|", "`", "$(", "\n", ">", "<", "&"]
    if any(tok in command for tok in danger_chars):
        fallthrough()
        return

    try:
        tokens = shlex.split(command, comments=False, posix=True)
    except ValueError:
        # 引號沒配對之類的解析錯誤，一律不處理
        fallthrough()
        return

    if len(tokens) != 2 or tokens[0] != "cd":
        fallthrough()
        return

    target = tokens[1]

    # 排除絕對路徑、家目錄、cd - 等
    if target.startswith("/") or target.startswith("~") or target == "-":
        fallthrough()
        return

    # 正規化並檢查每一段路徑
    parts = [p for p in target.split("/") if p not in ("", ".")]
    if not parts:
        fallthrough()
        return

    for p in parts:
        if p == "..":
            fallthrough()
            return
        if p.startswith("."):
            fallthrough()
            return

    # 如果 hook 收到 cwd，額外用 realpath 驗證，避免 symlink 逃逸
    cwd = event.get("cwd")
    if isinstance(cwd, str) and cwd:
        try:
            base = os.path.realpath(cwd)
            resolved = os.path.realpath(os.path.join(cwd, target))
            if not (resolved == base or resolved.startswith(base + os.sep)):
                fallthrough()
                return
        except Exception:
            fallthrough()
            return

    allow(f"自動放行：cd 進入非隱藏子目錄 '{target}'")


if __name__ == "__main__":
    main()
