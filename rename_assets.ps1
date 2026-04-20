
# HyTide Asset Rename Script
# Renames all C_ prefixed assets and base game overrides to HyTide_ prefix
# Also updates all internal references

$base = "c:\Users\bswea\Projects\Hytale Plugin 1 (Vermintide Spawns)\src\main\resources\Server"

# ============================================================
# STEP 1: Rename NPC Role files (C_ -> HyTide_)
# ============================================================
Write-Host "=== STEP 1: Renaming NPC Role files ===" -ForegroundColor Cyan
$rolesDir = "$base\NPC\Roles"
Get-ChildItem -Path $rolesDir -Filter "C_*.json" -File | ForEach-Object {
    $newName = $_.Name -replace "^C_", "HyTide_"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# ============================================================
# STEP 2: Rename NPC Template files
# ============================================================
Write-Host "`n=== STEP 2: Renaming NPC Template files ===" -ForegroundColor Cyan
$templatesDir = "$base\NPC\Roles\Templates"
Get-ChildItem -Path $templatesDir -Filter "*.json" -File | ForEach-Object {
    $newName = "HyTide_$($_.Name)"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# ============================================================
# STEP 3: Rename custom weapon files (C_ -> HyTide_)
# ============================================================
Write-Host "`n=== STEP 3: Renaming custom weapon files ===" -ForegroundColor Cyan
Get-ChildItem -Path "$base\Item\Items" -Recurse -Filter "C_*.json" -File | ForEach-Object {
    $newName = $_.Name -replace "^C_", "HyTide_"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# ============================================================
# STEP 4: Rename referenced weapon interaction files
# ============================================================
Write-Host "`n=== STEP 4: Renaming weapon interaction files ===" -ForegroundColor Cyan

# These are the specific interaction files referenced by custom weapons
$interactionRenames = @(
    # Battleaxe
    "Weapon_Battleaxe_Primary_Swing_Down_Damage"
    "Weapon_Battleaxe_Primary_Swing_Down_Left_Damage"
    "Weapon_Battleaxe_Primary_Swing_Down_Right_Damage"
    "Weapon_Battleaxe_Primary_Downstrike_Damage"
    "Weapon_Battleaxe_Signature_Whirlwind_Damage"
    "Weapon_Battleaxe_Secondary_Guard_Wield"
    # Sword
    "Weapon_Sword_Primary_Swing_Left_Damage"
    "Weapon_Sword_Primary_Swing_Right_Damage"
    "Weapon_Sword_Primary_Swing_Down_Damage"
    "Weapon_Sword_Primary_Thrust_Damage"
    "Weapon_Sword_Signature_Vortexstrike_Spin_Damage"
    "Weapon_Sword_Signature_Vortexstrike_Stab_Damage"
    "Weapon_Sword_Secondary_Guard_Wield"
    # Daggers
    "Weapon_Daggers_Primary_Swing_Left_Damage"
    "Weapon_Daggers_Primary_Swing_Right_Damage"
    "Weapon_Daggers_Primary_Stab_Left_Damage"
    "Weapon_Daggers_Primary_Stab_Right_Damage"
    "Weapon_Daggers_Primary_Pounce_Sweep_Damage"
    "Weapon_Daggers_Primary_Pounce_Stab_Damage"
    "Weapon_Daggers_Signature_Razorstrike_Slash_Damage"
    "Weapon_Daggers_Signature_Razorstrike_Sweep_Damage"
    "Weapon_Daggers_Signature_Razorstrike_Lunge_Damage"
    "Weapon_Daggers_Secondary_Guard_Wield"
    # Mace
    "Weapon_Mace_Primary_Swing_Left_Damage"
    "Weapon_Mace_Primary_Swing_Right_Damage"
    "Weapon_Mace_Primary_Swing_Up_Left_Damage"
    "Weapon_Mace_Primary_Swing_Left_Charged_Damage"
    "Weapon_Mace_Primary_Swing_Right_Charged_Damage"
    "Weapon_Mace_Primary_Swing_Up_Left_Charged_Damage"
    "Weapon_Mace_Signature_Groundslam_Damage"
    "Weapon_Mace_Secondary_Guard_Wield"
    # Shortbow
    "Weapon_Shortbow_Primary_Shoot_Damage_Strength_0"
    "Weapon_Shortbow_Primary_Shoot_Damage_Strength_1"
    "Weapon_Shortbow_Primary_Shoot_Damage_Strength_2"
    "Weapon_Shortbow_Primary_Shoot_Damage_Strength_3"
    "Weapon_Shortbow_Primary_Shoot_Damage_Strength_4"
    "Weapon_Shortbow_Signature_Volley_Damage"
    "Weapon_Shortbow_Secondary_Guard_Wield"
    # Shield
    "Weapon_Shield_Secondary_Guard_Wield"
    # Crossbow
    "Weapon_Crossbow_Damage_Standard_Projectile"
    "Weapon_Crossbow_Damage_Combo_Projectile"
    "Weapon_Crossbow_Damage_Signature_Projectile"
    "Weapon_Crossbow_Secondary_Guard_Wield"
)

$interactionsDir = "$base\Item\Interactions\Weapons"
foreach ($name in $interactionRenames) {
    $found = Get-ChildItem -Path $interactionsDir -Recurse -Filter "$name.json" -File
    if ($found) {
        foreach ($f in $found) {
            $newName = "HyTide_$($f.Name)"
            Write-Host "  $($f.Name) -> $newName"
            Rename-Item $f.FullName -NewName $newName
        }
    } else {
        Write-Host "  WARNING: $name.json not found!" -ForegroundColor Yellow
    }
}

# ============================================================
# STEP 5: Rename NPC interaction files
# ============================================================
Write-Host "`n=== STEP 5: Renaming NPC interaction files ===" -ForegroundColor Cyan
$npcInteractionsDir = "$base\Item\Interactions\NPCs"
Get-ChildItem -Path $npcInteractionsDir -Recurse -Filter "*.json" -File | ForEach-Object {
    $newName = "HyTide_$($_.Name)"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# Also rename Explode_Generic
$explodeFile = "$base\Item\Interactions\Explosions\Explode_Generic.json"
if (Test-Path $explodeFile) {
    Write-Host "  Explode_Generic.json -> HyTide_Explode_Generic.json"
    Rename-Item $explodeFile -NewName "HyTide_Explode_Generic.json"
}

# ============================================================
# STEP 6: Rename balancing files
# ============================================================
Write-Host "`n=== STEP 6: Renaming balancing files ===" -ForegroundColor Cyan
$balancingDir = "$base\NPC\Balancing"
Get-ChildItem -Path $balancingDir -Recurse -Filter "*.json" -File | ForEach-Object {
    $newName = "HyTide_$($_.Name)"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# ============================================================
# STEP 7: Rename projectile files
# ============================================================
Write-Host "`n=== STEP 7: Renaming projectile files ===" -ForegroundColor Cyan
Get-ChildItem -Path "$base\Projectiles" -Recurse -Filter "*.json" -File | ForEach-Object {
    $newName = "HyTide_$($_.Name)"
    Write-Host "  $($_.Name) -> $newName"
    Rename-Item $_.FullName -NewName $newName
}

# ============================================================
# STEP 8: Rename attitude file (PlayerEnemy only)
# ============================================================
Write-Host "`n=== STEP 8: Renaming PlayerEnemy attitude ===" -ForegroundColor Cyan
$playerEnemyFile = "$base\NPC\Attitude\Roles\PlayerEnemy.json"
if (Test-Path $playerEnemyFile) {
    Write-Host "  PlayerEnemy.json -> HyTide_PlayerEnemy.json"
    Rename-Item $playerEnemyFile -NewName "HyTide_PlayerEnemy.json"
}

# ============================================================
# STEP 9: Rename _Core component
# ============================================================
Write-Host "`n=== STEP 9: Renaming _Core component ===" -ForegroundColor Cyan
$coreFile = "$base\NPC\Roles\_Core\Component_Instruction_Simple_Soft_Leash.json"
if (Test-Path $coreFile) {
    Write-Host "  Component_Instruction_Simple_Soft_Leash.json -> HyTide_Component_Instruction_Simple_Soft_Leash.json"
    Rename-Item $coreFile -NewName "HyTide_Component_Instruction_Simple_Soft_Leash.json"
}

# ============================================================
# STEP 10: Rename override item files
# ============================================================
Write-Host "`n=== STEP 10: Renaming override item files ===" -ForegroundColor Cyan
$overrideItems = @(
    "$base\Item\Items\Weapon\Arrow\Weapon_Arrow_Crude.json"
    "$base\Item\Items\Weapon\Bomb\Weapon_Bomb.json"
    "$base\Item\Items\Potion\Potion_Health_Large.json"
    "$base\Item\Items\Furniture\Human\Unique\Furniture_Human_Ruins_Door_Large.json"
)
foreach ($item in $overrideItems) {
    if (Test-Path $item) {
        $leaf = Split-Path $item -Leaf
        $newName = "HyTide_$leaf"
        Write-Host "  $leaf -> $newName"
        Rename-Item $item -NewName $newName
    } else {
        Write-Host "  WARNING: $item not found!" -ForegroundColor Yellow
    }
}

Write-Host "`n=== File renames complete ===" -ForegroundColor Green
Write-Host "Now updating internal references..." -ForegroundColor Cyan

# ============================================================
# STEP 11: Update all internal JSON references
# ============================================================

# Build a mapping of old name -> new name for content replacement
# We process longest names first to avoid partial replacements
$replacements = [ordered]@{}

# Template renames
$replacements["Template_Aggressive_Zombies_Burnt_Amb"] = "HyTide_Template_Aggressive_Zombies_Burnt_Amb"
$replacements["Template_Aggressive_Zombies_Amb"] = "HyTide_Template_Aggressive_Zombies_Amb"
$replacements["Template_Scarak_Broodmother"] = "HyTide_Template_Scarak_Broodmother"
$replacements["Template_Goblin_Scavenger"] = "HyTide_Template_Goblin_Scavenger"
$replacements["Template_Goblin_Scrapper"] = "HyTide_Template_Goblin_Scrapper"
$replacements["Template_Goblin_Lobber"] = "HyTide_Template_Goblin_Lobber"
$replacements["Template_Scarak_Louse"] = "HyTide_Template_Scarak_Louse"
$replacements["Template_Intelligent"] = "HyTide_Template_Intelligent"
$replacements["Template_Trork_Melee"] = "HyTide_Template_Trork_Melee"
$replacements["Template_Predator"] = "HyTide_Template_Predator"
$replacements["Template_Eye"] = "HyTide_Template_Eye"
$replacements["Bear_Grizzly"] = "HyTide_Bear_Grizzly"

# Attitude rename
$replacements['"AttitudeGroup": "PlayerEnemy"'] = '"AttitudeGroup": "HyTide_PlayerEnemy"'

# CAE renames (CombatConfig references)
$replacements["CAE_Goblin_Scavenger"] = "HyTide_CAE_Goblin_Scavenger"
$replacements["CAE_Goblin_Scrapper"] = "HyTide_CAE_Goblin_Scrapper"
$replacements["CAE_Hedera"] = "HyTide_CAE_Hedera"

# Component rename
$replacements["Component_Instruction_Simple_Soft_Leash"] = "HyTide_Component_Instruction_Simple_Soft_Leash"

# NPC Interaction renames
$replacements["Goblin_Lobber_Bomb_Explode_Charged"] = "HyTide_Goblin_Lobber_Bomb_Explode_Charged"
$replacements["Goblin_Lobber_Bomb_Explode"] = "HyTide_Goblin_Lobber_Bomb_Explode"
$replacements["Hedera_Scream_Damage"] = "HyTide_Hedera_Scream_Damage"
$replacements["Explode_Generic"] = "HyTide_Explode_Generic"

# C_ -> HyTide_ for NPC role references (in FlockArrays, Spawn fields, etc.)
# Process all C_ references - this covers NPC roles in JSON files
# We need to be careful to only match full identifiers

# Get all JSON files under Server/
$allJsonFiles = Get-ChildItem -Path $base -Recurse -Filter "*.json" -File

$updateCount = 0
foreach ($file in $allJsonFiles) {
    $content = Get-Content $file.FullName -Raw
    $original = $content
    
    # Replace C_ prefixed names with HyTide_ (for NPC roles referenced in JSON)
    $content = $content -replace '"C_', '"HyTide_'
    
    # Replace template references
    foreach ($key in $replacements.Keys) {
        $content = $content -replace [regex]::Escape($key), $replacements[$key]
    }
    
    # Replace interaction file references (weapon damage files)
    foreach ($name in $interactionRenames) {
        $content = $content -replace [regex]::Escape($name), "HyTide_$name"
    }
    
    if ($content -ne $original) {
        Set-Content $file.FullName -Value $content -NoNewline
        $updateCount++
        Write-Host "  Updated: $($file.FullName)" -ForegroundColor DarkGray
    }
}

Write-Host "`n=== Updated $updateCount JSON files ===" -ForegroundColor Green
Write-Host "`nDone! File renames and reference updates complete." -ForegroundColor Green
