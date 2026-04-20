$ErrorActionPreference = "Stop"
$projectRoot = "c:\Users\bswea\Projects\Hytale Plugin 1 (Vermintide Spawns)"
$interactionsDir = "$projectRoot\src\main\resources\Server\Item\Interactions"
$itemsDir = "$projectRoot\src\main\resources\Server\Item\Items"
$resourcesDir = "$projectRoot\src\main\resources\Server"

# ============================================================
# STEP 1: Rename all non-HyTide, non-Deprecated interaction files
# ============================================================
Write-Host "=== STEP 1: Renaming interaction files ===" -ForegroundColor Cyan

$filesToRename = Get-ChildItem $interactionsDir -Recurse -File | Where-Object {
    $_.Name -notlike "HyTide_*" -and 
    $_.Name -notlike "Deprecated*" -and
    $_.Directory.Name -ne "Deprecated"
}

$renameMap = @{}
foreach ($file in $filesToRename) {
    $oldName = $file.BaseName
    $newName = "HyTide_$oldName"
    $renameMap[$oldName] = $newName
    
    $newFileName = "HyTide_$($file.Name)"
    $newPath = Join-Path $file.DirectoryName $newFileName
    
    if (Test-Path $newPath) {
        Write-Host "  SKIP (exists): $($file.Name)" -ForegroundColor Yellow
    } else {
        Rename-Item $file.FullName -NewName $newFileName
        Write-Host "  Renamed: $($file.Name) -> $newFileName" -ForegroundColor Green
    }
}
Write-Host "Renamed $($renameMap.Count) files`n" -ForegroundColor Cyan

# ============================================================
# STEP 2: Create HyTide_ Root files
# ============================================================
Write-Host "=== STEP 2: Creating HyTide_ Root files ===" -ForegroundColor Cyan

$rootFileMap = @{
"Weapons\Battleaxe\HyTide_Root_Weapon_Battleaxe_Primary.json" = '{
  "RequireNewClick": true,
  "ClickQueuingTimeout": 0.3,
  "Cooldown": { "Cooldown": 0.75 },
  "Interactions": ["HyTide_Weapon_Battleaxe_Primary"],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Battleaxe\HyTide_Root_Weapon_Battleaxe_Secondary_Guard.json" = '{
  "RequireNewClick": true,
  "Rules": { "Interrupting": ["Primary"] },
  "Interactions": [{ "Type": "Replace", "Var": "Guard_Start", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Battleaxe_Secondary_Guard"] } }],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Battleaxe\HyTide_Root_Weapon_Battleaxe_Signature_Whirlwind.json" = '{
  "RequireNewClick": true,
  "Interactions": ["HyTide_Weapon_Battleaxe_Signature_Whirlwind"]
}'
"Weapons\Daggers\HyTide_Root_Weapon_Daggers_Primary.json" = '{
  "RequireNewClick": true,
  "ClickQueuingTimeout": 0.15,
  "Cooldown": { "Cooldown": 0.2 },
  "Interactions": ["HyTide_Weapon_Daggers_Primary"]
}'
"Weapons\Daggers\HyTide_Root_Weapon_Daggers_Secondary_Guard.json" = '{
  "RequireNewClick": true,
  "Rules": { "Interrupting": ["Primary"] },
  "Interactions": [{ "Type": "Replace", "Var": "Guard_Start", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Daggers_Secondary_Guard"] } }],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Daggers\HyTide_Root_Weapon_Daggers_Signature_Razorstrike.json" = '{
  "RequireNewClick": true,
  "Interactions": [{ "Type": "Replace", "Var": "Signature_Razorstrike", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Daggers_Signature_Razorstrike"] } }]
}'
"Weapons\Mace\HyTide_Root_Weapon_Mace_Primary.json" = '{
  "RequireNewClick": true,
  "ClickQueuingTimeout": 0.3,
  "Cooldown": { "Cooldown": 0.75 },
  "Interactions": ["HyTide_Weapon_Mace_Primary"],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Mace\HyTide_Root_Weapon_Mace_Secondary_Guard.json" = '{
  "RequireNewClick": true,
  "Rules": { "Interrupting": ["Primary"] },
  "Interactions": [{ "Type": "Replace", "Var": "Guard_Start", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Mace_Secondary_Guard"] } }],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Mace\HyTide_Root_Weapon_Mace_Signature_Groundslam.json" = '{
  "Interactions": ["HyTide_Weapon_Mace_Signature_Groundslam"],
  "Tags": { "Attack": ["Melee"] },
  "RequireNewClick": true
}'
"Weapons\Shield\HyTide_Root_Weapon_Shield_Secondary_Guard.json" = '{
  "RequireNewClick": true,
  "Rules": { "Interrupting": ["Primary"] },
  "Interactions": [{ "Type": "Replace", "Var": "Guard_Start", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Shield_Secondary_Guard"] } }],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Sword\HyTide_Root_Weapon_Sword_Primary.json" = '{
  "RequireNewClick": true,
  "ClickQueuingTimeout": 0.2,
  "Cooldown": { "Cooldown": 0.25 },
  "Interactions": ["HyTide_Weapon_Sword_Primary"]
}'
"Weapons\Sword\HyTide_Root_Weapon_Sword_Secondary_Guard.json" = '{
  "RequireNewClick": true,
  "Rules": { "Interrupting": ["Primary"] },
  "Interactions": [{ "Type": "Replace", "Var": "Guard_Start", "DefaultOk": true, "DefaultValue": { "Interactions": ["HyTide_Weapon_Sword_Secondary_Guard"] } }],
  "Tags": { "Attack": ["Melee"] }
}'
"Weapons\Sword\HyTide_Root_Weapon_Sword_Signature_Vortexstrike.json" = '{
  "RequireNewClick": true,
  "Interactions": ["HyTide_Weapon_Sword_Signature_Vortexstrike"]
}'
}

$rootCreated = 0
foreach ($relPath in $rootFileMap.Keys) {
    $fullPath = Join-Path $interactionsDir $relPath
    $dir = Split-Path $fullPath -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    if (Test-Path $fullPath) {
        Write-Host "  SKIP (exists): $relPath" -ForegroundColor Yellow
    } else {
        Set-Content -Path $fullPath -Value $rootFileMap[$relPath] -Encoding UTF8
        Write-Host "  Created: $relPath" -ForegroundColor Green
        $rootCreated++
    }
}
Write-Host "Created $rootCreated Root files`n" -ForegroundColor Cyan

# ============================================================
# STEP 3: Update all internal references in ALL JSON files
# ============================================================
Write-Host "=== STEP 3: Updating internal references ===" -ForegroundColor Cyan

$sortedKeys = $renameMap.Keys | Sort-Object -Property Length -Descending
$allJsonFiles = Get-ChildItem $resourcesDir -Recurse -File -Filter "*.json"

$totalUpdated = 0
foreach ($jsonFile in $allJsonFiles) {
    $content = Get-Content $jsonFile.FullName -Raw
    if (-not $content) { continue }
    $originalContent = $content
    
    foreach ($oldName in $sortedKeys) {
        $newName = $renameMap[$oldName]
        $escapedOld = [regex]::Escape($oldName)
        $content = $content -replace "`"$escapedOld`"", "`"$newName`""
    }
    
    if ($content -ne $originalContent) {
        Set-Content -Path $jsonFile.FullName -Value $content -Encoding UTF8 -NoNewline
        $totalUpdated++
        Write-Host "  Updated: $($jsonFile.Name)" -ForegroundColor Green
    }
}
Write-Host "Updated references in $totalUpdated files`n" -ForegroundColor Cyan

# ============================================================
# STEP 4: Add Interactions overrides to custom weapon files
# ============================================================
Write-Host "=== STEP 4: Adding Interactions to custom weapons ===" -ForegroundColor Cyan

$weaponOverrides = @(
    @{ File="Weapon\Battleaxe\HyTide_Weapon_Battleaxe_Mithril.json"; Primary="HyTide_Root_Weapon_Battleaxe_Primary"; Secondary="HyTide_Root_Weapon_Battleaxe_Secondary_Guard"; Ability1="HyTide_Root_Weapon_Battleaxe_Signature_Whirlwind" }
    @{ File="Weapon\Daggers\HyTide_Weapon_Daggers_Mithril.json"; Primary="HyTide_Root_Weapon_Daggers_Primary"; Secondary="HyTide_Root_Weapon_Daggers_Secondary_Guard"; Ability1="HyTide_Root_Weapon_Daggers_Signature_Razorstrike" }
    @{ File="Weapon\Mace\HyTide_Weapon_Mace_Mithril.json"; Primary="HyTide_Root_Weapon_Mace_Primary"; Secondary="HyTide_Root_Weapon_Mace_Secondary_Guard"; Ability1="HyTide_Root_Weapon_Mace_Signature_Groundslam" }
    @{ File="Weapon\Shield\HyTide_Weapon_Shield_Mithril.json"; Secondary="HyTide_Root_Weapon_Shield_Secondary_Guard" }
    @{ File="Weapon\Sword\HyTide_Weapon_Sword_Mithril.json"; Primary="HyTide_Root_Weapon_Sword_Primary"; Secondary="HyTide_Root_Weapon_Sword_Secondary_Guard"; Ability1="HyTide_Root_Weapon_Sword_Signature_Vortexstrike" }
)

foreach ($w in $weaponOverrides) {
    $filePath = Join-Path $itemsDir $w.File
    if (-not (Test-Path $filePath)) {
        Write-Host "  NOT FOUND: $($w.File)" -ForegroundColor Red
        continue
    }
    
    $content = Get-Content $filePath -Raw
    
    # Build Interactions block
    $lines = @()
    if ($w.Primary)   { $lines += "    `"Primary`": `"$($w.Primary)`"" }
    if ($w.Secondary) { $lines += "    `"Secondary`": `"$($w.Secondary)`"" }
    if ($w.Ability1)  { $lines += "    `"Ability1`": `"$($w.Ability1)`"" }
    $block = "  `"Interactions`": {`n" + ($lines -join ",`n") + "`n  },"
    
    if ($content -match '"Interactions"\s*:') {
        Write-Host "  SKIP (already has Interactions): $($w.File)" -ForegroundColor Yellow
        continue
    }
    
    # Insert after "Parent" line
    $content = $content -replace '("Parent"\s*:\s*"[^"]+"\s*,)', "`$1`n$block"
    Set-Content -Path $filePath -Value $content -Encoding UTF8 -NoNewline
    Write-Host "  Added Interactions: $($w.File)" -ForegroundColor Green
}

Write-Host "`n=== COMPLETE ===" -ForegroundColor Cyan
Write-Host "  Renamed: $($renameMap.Count) files"
Write-Host "  Root files: $rootCreated"
Write-Host "  Ref updates: $totalUpdated files"
Write-Host "  Weapons updated: $($weaponOverrides.Count)"
