param([string]$InstanceDirectory)
$ErrorActionPreference='Stop'
if(-not $InstanceDirectory){
 $default=Join-Path $env:APPDATA 'ElyPrismLauncher\instances\ZombKotom'
 if(Test-Path -LiteralPath (Join-Path $default 'instance.cfg')){$InstanceDirectory=$default}
 else {$InstanceDirectory=Read-Host 'Paste your ZombKotom INSTANCE folder (contains instance.cfg)'}
}
$instance=(Resolve-Path -LiteralPath $InstanceDirectory.Trim('"')).Path
$cfg=Join-Path $instance 'instance.cfg'
if(-not(Test-Path -LiteralPath $cfg)){throw 'Choose the instance folder containing instance.cfg, not the mods folder.'}
if(Get-Process javaw -ErrorAction SilentlyContinue){throw 'Save and close Minecraft before setup.'}
if(Get-Process elyprismlauncher,prismlauncher,pineconemc -ErrorAction SilentlyContinue){throw 'Exit Pinecone completely, including its tray icon, before setup.'}
$game=Join-Path $instance 'minecraft'
if(-not(Test-Path -LiteralPath $game)){$game=Join-Path $instance '.minecraft'}
if(-not(Test-Path -LiteralPath $game)){throw 'Minecraft folder was not found in the instance.'}
$backup=Join-Path $instance ('autoupdate-setup-backup-'+[DateTime]::Now.ToString('yyyyMMdd-HHmmss'))
New-Item -ItemType Directory -Path $backup | Out-Null
Copy-Item -LiteralPath $cfg -Destination (Join-Path $backup 'instance.cfg')
foreach($n in @('lastbastion-update.ps1','packwiz-installer-bootstrap.jar','packwiz-installer.jar','lastbastion-tools.json','lastbastion-retired-mods.json','lastbastion-initial-downloads.json')){
 $dest=Join-Path $game $n
 if(Test-Path -LiteralPath $dest){Copy-Item -LiteralPath $dest -Destination (Join-Path $backup $n)}
 Copy-Item -LiteralPath (Join-Path $PSScriptRoot $n) -Destination $dest -Force
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$defaults=[IO.Compression.ZipFile]::OpenRead((Join-Path $PSScriptRoot 'personal-defaults.zip'))
try{
 foreach($entry in $defaults.Entries){
  if(-not $entry.Name){continue}
  $dest=[IO.Path]::GetFullPath((Join-Path $game $entry.FullName))
  if(-not $dest.StartsWith($game.TrimEnd('\')+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid defaults path'}
  if(-not(Test-Path -LiteralPath $dest)){
   New-Item -ItemType Directory -Path ([IO.Path]::GetDirectoryName($dest)) -Force | Out-Null
   [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$dest,$false)
  }
 }
}finally{$defaults.Dispose()}
$command='powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$INST_MC_DIR/lastbastion-update.ps1" -GameDirectory "$INST_MC_DIR" -JavaPath "$INST_JAVA"'
$value='"'+$command.Replace('\','\\').Replace('"','\"')+'"'
$settings=@{'OverrideCommands'='true';'PreLaunchCommand'=$value}
$lines=[Collections.Generic.List[string]]::new()
$general=$false;$added=$false
foreach($line in [IO.File]::ReadAllLines($cfg)){
 if($line -match '^\['){
  if($general -and -not $added){foreach($k in $settings.Keys){$lines.Add($k+'='+$settings[$k])};$added=$true}
  $general=$line -eq '[General]'
 }
 if($general -and $line -match '^(OverrideCommands|PreLaunchCommand)='){continue}
 $lines.Add($line)
}
if(-not $added){if(-not $general){$lines.Add('[General]')};foreach($k in $settings.Keys){$lines.Add($k+'='+$settings[$k])}}
[IO.File]::WriteAllLines($cfg,$lines,[Text.UTF8Encoding]::new($false))
Write-Output 'Auto-update enabled. Open your usual Pinecone and press Play.'
Write-Output 'Your worlds, controls, custom shaders and shader selection were preserved.'
Write-Output ('Original settings saved in: '+$backup)
