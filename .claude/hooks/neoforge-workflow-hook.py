#!/usr/bin/env python3
"""
Claude Code PreToolUse hook — NeoForge mod 開發常見指令自動放行。

設計目標:針對這個專案裡「每次都長得差不多、但因為路徑帶有 session UUID
或檔案清單而無法用靜態規則匹配」的指令，用程式邏輯辨識「安全的形狀」，
而不是辨識「安全的完整字串」。這樣即使 Claude Code 每次 session 換一個
暫存資料夾 UUID，規則依然有效。

安全原則(和先前的 cd hook 一致):
1. 任何指令只要包含 &&、||、;、|、反引號、$(、換行、重導向符號，一律
   視為「非單純指令」，不自動放行，交回原本的詢問流程。
2. 每一類指令都用白名單「形狀」比對(固定的指令名、固定的旗標集合、
   路徑必須落在允許的目錄前綴下)，不符合就交回詢問，絕不猜測。
3. 真正有破壞性的操作(rm -rf)只允許「完全等於」專案裡唯一那個已知、
   可被 gradle 重新產生的資料夾路徑，不接受萬用字元、不接受其他任何
   路徑，即使看起來很像。
4. 會執行任意程式碼的操作(python3 -c "...")一律不自動放行。
5. Fail-closed：任何解析失敗、格式不如預期，一律 fallthrough，讓
   Claude Code 用原本的規則/詢問流程處理。這支 hook 只會「多放行」
   明確安全的情況，不會「多擋下」或「放寬」任何其他指令。
"""

import json
import os
import re
import shlex
import sys

# ---------------------------------------------------------------------------
# 可依專案調整的白名單
# ---------------------------------------------------------------------------

# 允許自動放行的 gradlew 任務名稱。如果你常用其他任務，把它加進來即可。
SAFE_GRADLE_TASKS = {
    "compileJava",
    "processResources",
    "runData",
    "runDataServer",
    "tasks",
    "build",
}
# gradlew 指令本身允許的旗標(除了任務名稱以外，唯一接受的額外 token)
SAFE_GRADLE_FLAGS = {"--console=plain"}

# rm -rf 只允許精確等於這個相對路徑(相對於專案根目錄，也就是 cwd)。
# 這是 Minecraft 資料產生(data generation)每次都會重新產生的輸出目錄，
# 刪除它是安全、可逆的(重新跑 runData 就會生回來)。
SAFE_RM_RELATIVE_PATHS = {
    "src/generated/resources",
}

# python3 只允許執行專案自己 tools/ 目錄下的腳本檔(不接受 -c 任意程式碼)
SAFE_PYTHON_SCRIPT_DIR = "tools/"

# 允許自動放行 unzip 解壓縮目的地的路徑樣式：
#   1. Claude Code 自己建立的 scratchpad 暫存目錄(不管 session UUID 是什麼)
#   2. /tmp/ 底下
SCRATCH_PATTERN = re.compile(
    r"AppData/Local/Temp/claude/.*?/scratchpad(/|$)", re.IGNORECASE
)


# ---------------------------------------------------------------------------
# 工具函式
# ---------------------------------------------------------------------------

def fallthrough():
    """不做決定，交回 Claude Code 原本的權限流程。"""
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


def normalize(path: str) -> str:
    return path.replace("\\\\", "/").replace("\\", "/")


def is_scratch_path(path: str) -> bool:
    p = normalize(path)
    if SCRATCH_PATTERN.search(p):
        return True
    if p.startswith("/tmp/"):
        return True
    return False


def split_command(command: str):
    """用 posix=False 保留 Windows 路徑裡的反斜線，再自己去掉外層引號。"""
    try:
        tokens = shlex.split(command, posix=False)
    except ValueError:
        return None
    cleaned = []
    for t in tokens:
        if len(t) >= 2 and t[0] == t[-1] and t[0] in ("'", '"'):
            t = t[1:-1]
        cleaned.append(t)
    return cleaned


DANGER_SUBSTRINGS = ["&&", "||", ";", "|", "`", "$(", "\n", ">", "<", "&"]


def has_danger_operator(command: str) -> bool:
    return any(tok in command for tok in DANGER_SUBSTRINGS)


# ---------------------------------------------------------------------------
# 各類指令的判斷邏輯
# ---------------------------------------------------------------------------

def check_unzip(tokens):
    """unzip -l <jar>                     -> 允許(唯讀列出內容)
       unzip -p <jar>                     -> 允許(唯讀印出，不重導向)
       unzip [-o] [-q] <jar> <entries...> -d <scratch_dir> -> 允許(限定目的地)
       其他(例如沒有 -d 就解壓縮到目前目錄) -> 交回詢問
    """
    if tokens[0] != "unzip":
        return False

    mode_list = False
    mode_print = False
    dest = None
    positionals = []

    idx = 1
    while idx < len(tokens):
        t = tokens[idx]
        if t == "-l":
            mode_list = True
        elif t == "-p":
            mode_print = True
        elif t in ("-o", "-q"):
            pass
        elif t == "-d":
            idx += 1
            if idx >= len(tokens):
                return False  # -d 後面沒接路徑，格式不對
            dest = tokens[idx]
        elif t.startswith("-"):
            return False  # 不認識的旗標，一律不自動放行
        else:
            positionals.append(t)
        idx += 1

    if not positionals:
        return False
    jar_path, entries = positionals[0], positionals[1:]

    if mode_list and dest is None and not entries:
        allow(f"自動放行：unzip -l 唯讀列出 '{jar_path}' 的內容")
        return True

    if mode_print and dest is None and not entries:
        allow(f"自動放行：unzip -p 唯讀輸出 '{jar_path}' 的內容")
        return True

    if not mode_list and not mode_print and dest is not None:
        if is_scratch_path(dest):
            allow(f"自動放行：解壓縮 '{jar_path}' 到暫存目錄 '{dest}'")
            return True
        return False  # 目的地不在允許的暫存路徑內，交回詢問

    return False


def check_mkdir(tokens):
    """mkdir -p <一個路徑>，且路徑必須在 scratchpad 底下才自動放行。"""
    if tokens[0] != "mkdir":
        return False
    if len(tokens) != 3 or tokens[1] != "-p":
        return False
    path = tokens[2]
    if is_scratch_path(path):
        allow(f"自動放行：在暫存目錄下建立資料夾 '{path}'")
        return True
    return False


def check_rm(tokens, cwd):
    """rm -rf <一個路徑>，路徑必須精確等於白名單裡的相對路徑
       (可以是相對路徑本身，或是 cwd + 該相對路徑的絕對路徑)。
       不接受萬用字元、不接受多個路徑、不接受其他旗標。
    """
    if tokens[0] != "rm":
        return False

    rest = tokens[1:]
    has_r = has_f = False
    paths = []
    for t in rest:
        if t in ("-rf", "-fr"):
            has_r = has_f = True
        elif t == "-r":
            has_r = True
        elif t == "-f":
            has_f = True
        elif t.startswith("-"):
            return False  # 其他旗標一律不自動放行
        else:
            paths.append(t)

    if not (has_r and has_f):
        return False
    if len(paths) != 1:
        return False

    target = normalize(paths[0]).rstrip("/")
    if any(part == ".." for part in target.split("/")):
        return False

    for safe_rel in SAFE_RM_RELATIVE_PATHS:
        safe_rel_norm = safe_rel.rstrip("/")
        if target.lower() == safe_rel_norm.lower():
            allow(f"自動放行：刪除可重新產生的資料夾 '{paths[0]}'")
            return True
        if isinstance(cwd, str) and cwd:
            abs_safe = normalize(os.path.join(cwd, safe_rel_norm)).rstrip("/")
            if target.lower() == abs_safe.lower():
                allow(f"自動放行：刪除可重新產生的資料夾 '{paths[0]}'")
                return True

    return False


def check_gradlew(tokens):
    """./gradlew.bat <一個或多個白名單任務> [--console=plain]"""
    exe = tokens[0].lower()
    if exe not in ("./gradlew.bat", ".\\gradlew.bat", "gradlew.bat",
                   "./gradlew", ".\\gradlew", "gradlew"):
        return False

    task_tokens = []
    for t in tokens[1:]:
        if t in SAFE_GRADLE_FLAGS:
            continue
        task_tokens.append(t)

    if not task_tokens:
        return False
    if not all(t in SAFE_GRADLE_TASKS for t in task_tokens):
        return False

    allow(f"自動放行：gradlew 已核准任務 {task_tokens}")
    return True


def check_python(tokens):
    """python3 tools/xxx.py [任意參數]  -> 允許
       python3 -c "..."                 -> 一律不自動放行(任意程式碼)
    """
    if tokens[0] not in ("python3", "python"):
        return False
    if len(tokens) < 2:
        return False
    if tokens[1] == "-c":
        return False  # 任意程式碼片段，永遠交回詢問

    script = normalize(tokens[1])
    if script.startswith(SAFE_PYTHON_SCRIPT_DIR) or (
        "/" + SAFE_PYTHON_SCRIPT_DIR in script
    ):
        if ".." in script.split("/"):
            return False
        allow(f"自動放行：執行專案內腳本 '{tokens[1]}'")
        return True
    return False


# ---------------------------------------------------------------------------
# 主流程
# ---------------------------------------------------------------------------

def main():
    try:
        event = json.loads(sys.stdin.read())
    except Exception:
        fallthrough()
        return

    if event.get("tool_name") not in ("Bash", "PowerShell"):
        fallthrough()
        return

    command = (event.get("tool_input") or {}).get("command")
    if not isinstance(command, str) or not command.strip():
        fallthrough()
        return

    if has_danger_operator(command):
        fallthrough()
        return

    tokens = split_command(command)
    if not tokens:
        fallthrough()
        return

    cwd = event.get("cwd")

    for checker in (
        lambda: check_unzip(tokens),
        lambda: check_mkdir(tokens),
        lambda: check_rm(tokens, cwd),
        lambda: check_gradlew(tokens),
        lambda: check_python(tokens),
    ):
        if checker():
            return  # 已在 checker 內部呼叫 allow() 並 exit

    fallthrough()


if __name__ == "__main__":
    main()
