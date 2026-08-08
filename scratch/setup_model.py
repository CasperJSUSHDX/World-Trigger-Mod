import json
import shutil
import os

model_path = r"C:\Users\caspe\.gemini\antigravity\brain\227ef1e2-e277-4895-9980-8cb871484bcd\.user_uploaded\media_1786219946567.json"
texture_path = r"C:\Users\caspe\.gemini\antigravity\brain\227ef1e2-e277-4895-9980-8cb871484bcd\.user_uploaded\media_1786219946586.png"

# Target paths
target_model = r"d:\Project\World-Trigger-Mod\src\main\resources\assets\wtmod\models\block\assembly_bench.json"
target_empty = r"d:\Project\World-Trigger-Mod\src\main\resources\assets\wtmod\models\block\assembly_bench_empty.json"
target_texture = r"d:\Project\World-Trigger-Mod\src\main\resources\assets\wtmod\textures\block\assembly_bench.png"
target_blockstate = r"d:\Project\World-Trigger-Mod\src\main\resources\assets\wtmod\blockstates\assembly_bench.json"

os.makedirs(os.path.dirname(target_model), exist_ok=True)
os.makedirs(os.path.dirname(target_texture), exist_ok=True)
os.makedirs(os.path.dirname(target_blockstate), exist_ok=True)

# 1. Update the texture reference in the model and save
with open(model_path, 'r', encoding='utf-8') as f:
    model_data = json.load(f)

# The user's JSON has textures mapping. We map them to wtmod:block/assembly_bench
if "textures" in model_data:
    for key in model_data["textures"]:
        model_data["textures"][key] = "wtmod:block/assembly_bench"
else:
    model_data["textures"] = {"0": "wtmod:block/assembly_bench", "particle": "wtmod:block/assembly_bench"}

with open(target_model, 'w', encoding='utf-8') as f:
    json.dump(model_data, f, indent=4)

# 2. Copy the texture
shutil.copy(texture_path, target_texture)

# 3. Create empty model
empty_model = {
    "textures": {
        "particle": "wtmod:block/assembly_bench"
    },
    "elements": []
}
with open(target_empty, 'w', encoding='utf-8') as f:
    json.dump(empty_model, f, indent=4)

# 4. Create blockstate
blockstate = {
    "variants": {
        "facing=north,half=left": { "model": "wtmod:block/assembly_bench", "y": 270 },
        "facing=east,half=left": { "model": "wtmod:block/assembly_bench", "y": 0 },
        "facing=south,half=left": { "model": "wtmod:block/assembly_bench", "y": 90 },
        "facing=west,half=left": { "model": "wtmod:block/assembly_bench", "y": 180 },
        
        "facing=north,half=right": { "model": "wtmod:block/assembly_bench_empty" },
        "facing=east,half=right": { "model": "wtmod:block/assembly_bench_empty" },
        "facing=south,half=right": { "model": "wtmod:block/assembly_bench_empty" },
        "facing=west,half=right": { "model": "wtmod:block/assembly_bench_empty" }
    }
}
with open(target_blockstate, 'w', encoding='utf-8') as f:
    json.dump(blockstate, f, indent=4)

print("Setup complete.")
